package di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import networking.DevengNetworkingModule
import networking.util.createHttpClient

internal actual object NetworkModule {
    actual val httpClient: HttpClient by lazy {
        if (!DevengNetworkingModule.loggingEnabled) {
            suppressHttpLogs()
        }
        createHttpClient(Js.create())
    }
}

private fun suppressHttpLogs() {
    js(
        """
        (function() {
                    const asciiText = `
██████╗ ███████╗██╗   ██╗███████╗███╗   ██╗ ██████╗ 
██╔══██╗██╔════╝██║   ██║██╔════╝████╗  ██║██╔════╝ 
██║  ██║█████╗  ██║   ██║█████╗  ██╔██╗ ██║██║  ███╗
██║  ██║██╔══╝  ╚██╗ ██╔╝██╔══╝  ██║╚██╗██║██║   ██║
██████╔╝███████╗ ╚████╔╝ ███████╗██║ ╚████║╚██████╔╝
╚═════╝ ╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝ ╚═════╝ 
        `;
        console.log(asciiText);
            
            // Store original console methods
            const originalConsoleError = console.error;
            const originalConsoleLog = console.log;
            
            // The key patterns that identify HTTP request/response logs
            const httpPatterns = [
                'https://', // Catches all HTTP URLs
                'Expected response body',
                'NoTransformationFoundException',
                'HttpClient:'
            ];
            
            // Helper function to check if a message contains HTTP-related content
            function isHttpRelated(message) {
                if (typeof message !== 'string') {
                    message = String(message || '');
                }
                return httpPatterns.some(pattern => message.includes(pattern));
            }
            
            // Filter HTTP-related error messages
            console.error = function() {
                if (arguments.length > 0 && isHttpRelated(arguments[0])) {
                    return; // Skip HTTP-related error messages
                }
                // Pass through all other error messages
                return originalConsoleError.apply(console, arguments);
            };
            
            // Filter HTTP-related log messages
            console.log = function() {
                if (arguments.length > 0 && isHttpRelated(arguments[0])) {
                    return; // Skip HTTP-related log messages
                }
                // Pass through all other log messages
                return originalConsoleLog.apply(console, arguments);
            };
            
            // Override fetch to prevent network error logging
            const originalFetch = window.fetch;
            window.fetch = function() {
                // Temporarily disable console.error during fetch operations
                const previousErrorFn = console.error;
                console.error = function() {};
                
                try {
                    const result = originalFetch.apply(this, arguments);
                    
                    // Restore console.error asynchronously after fetch completes
                    setTimeout(() => { console.error = previousErrorFn; }, 0);
                    
                    return result;
                } catch (e) {
                    // Restore console.error if fetch throws synchronously
                    console.error = previousErrorFn;
                    throw e;
                }
            };
        })();
    """
    )
}