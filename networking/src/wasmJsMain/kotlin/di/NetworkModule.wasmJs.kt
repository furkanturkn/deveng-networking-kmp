package di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import networking.DevengNetworkingModule
import networking.util.createHttpClient

internal actual object NetworkModule {
    init {
        if (!DevengNetworkingModule.loggingEnabled) {
            disableJsLogging()
        }
    }

    actual val httpClient: HttpClient by lazy {
        createHttpClient(Js.create())
    }
}

private fun disableJsLogging() {
    js(
        """
        console.log = function() {};
        console.debug = function() {};
        """
    )
}