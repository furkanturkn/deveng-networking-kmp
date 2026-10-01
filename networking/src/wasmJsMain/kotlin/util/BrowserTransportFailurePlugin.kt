package networking.util

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.io.IOException

private const val BROWSER_TRANSPORT_FAILURE_PLUGIN_NAME = "DevengBrowserTransportFailurePlugin"
private const val BROWSER_TRANSPORT_FAILURE_MESSAGE = "The browser could not complete the request."

/** A request the browser's fetch could not complete: offline, a DNS failure, a refused or dropped connection. */
public class BrowserTransportFailureException(cause: Throwable) : IOException(BROWSER_TRANSPORT_FAILURE_MESSAGE, cause)

/**
 * Turns the browser engine's failed fetch into an exception callers can catch.
 *
 * Ktor's browser engine reports a failed fetch as `kotlin.Error("Fail to fetch")`, which is not an [Exception], so it
 * slipped past every `catch (exception: Exception)` in [networking.DevengNetworkingModule] and in the apps: the calling
 * coroutine died without a [error_handling.DevengException]. Lives in the wasmJs source set only, because on the
 * JVM an [Error] is a genuine VM failure that must not become a retryable one.
 */
internal fun buildBrowserTransportFailurePlugin(): ClientPlugin<Unit> =
    createClientPlugin(BROWSER_TRANSPORT_FAILURE_PLUGIN_NAME) {
        on(Send) { request ->
            try {
                proceed(request)
            } catch (fetchFailure: Error) {
                throw BrowserTransportFailureException(fetchFailure)
            }
        }
    }
