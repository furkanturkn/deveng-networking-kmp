package di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import networking.DevengNetworkingConfig
import networking.session.RefreshCoordinator
import networking.util.createHttpClient

internal actual object NetworkModule {
    actual fun createHttpClient(
        config: DevengNetworkingConfig,
        currentAccessToken: () -> String,
        refreshCoordinator: RefreshCoordinator?
    ): HttpClient {
        return createHttpClient(OkHttp.create(), config, currentAccessToken, refreshCoordinator)
    }
}