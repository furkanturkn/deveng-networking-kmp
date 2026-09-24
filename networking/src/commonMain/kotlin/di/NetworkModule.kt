package di

import io.ktor.client.HttpClient
import networking.DevengNetworkingConfig
import networking.session.RefreshCoordinator

internal expect object NetworkModule {
    fun createHttpClient(
        config: DevengNetworkingConfig,
        currentAccessToken: () -> String,
        refreshCoordinator: RefreshCoordinator?
    ): HttpClient
}