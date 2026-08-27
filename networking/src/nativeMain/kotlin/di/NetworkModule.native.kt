package di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import networking.DevengNetworkingConfig
import networking.util.createHttpClient

internal actual object NetworkModule {
    actual fun createHttpClient(
        config: DevengNetworkingConfig,
        currentAccessToken: () -> String
    ): HttpClient {
        return createHttpClient(Darwin.create(), config, currentAccessToken)
    }
}