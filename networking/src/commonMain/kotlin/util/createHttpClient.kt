package networking.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import networking.DevengNetworkingConfig
import networking.di.CoreModule

internal fun createHttpClient(
    engine: HttpClientEngine,
    config: DevengNetworkingConfig = DevengNetworkingConfig()
): HttpClient {
    return HttpClient(engine) {
        if (config.loggingEnabled) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }

                }
                level = LogLevel.ALL
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMillis
            connectTimeoutMillis = config.connectTimeoutMillis
            socketTimeoutMillis = config.socketTimeoutMillis
        }

        install(ContentNegotiation) {
            json(CoreModule.sharedJson)
        }

        install(WebSockets)
    }
}