package di

import io.ktor.client.HttpClient
import networking.DevengNetworkingConfig

internal expect object NetworkModule {
    fun createHttpClient(config: DevengNetworkingConfig): HttpClient
}