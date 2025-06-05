package networking.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import networking.DevengNetworkingModule
import networking.di.CoreModule

internal fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        if (DevengNetworkingModule.loggingEnabled) {
            install(Logging) {
                level = LogLevel.ALL
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = DevengNetworkingModule.requestTimeoutMillis
            connectTimeoutMillis = DevengNetworkingModule.connectTimeoutMillis
            socketTimeoutMillis = DevengNetworkingModule.socketTimeoutMillis
        }

        install(ContentNegotiation) {
            json(CoreModule.sharedJson)
        }

        install(WebSockets)
    }
}