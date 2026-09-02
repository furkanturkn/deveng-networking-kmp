package networking.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.currentCoroutineContext
import networking.DevengNetworkingConfig
import networking.csrf.DevengCsrfTokenProvider
import networking.di.CoreModule
import networking.session.RefreshCoordinator
import networking.session.RefreshGuard

internal fun createHttpClient(
    engine: HttpClientEngine,
    config: DevengNetworkingConfig = DevengNetworkingConfig(),
    currentAccessToken: () -> String
): HttpClient {
    val client = HttpClient(engine) {
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

        if (config.sessionRefresher != null) {
            install(HttpSend)
        }

        config.csrfTokenProvider?.let { provider ->
            install(buildCsrfPlugin(headerName = config.csrfHeaderName, provider = provider))
        }

        config.httpClientConfig?.invoke(this)
    }

    config.sessionRefresher?.let { refresher ->
        val refreshCoordinator = RefreshCoordinator(
            refresher = refresher,
            refreshTimeoutMillis = config.refreshTimeoutMillis
        )

        client.plugin(HttpSend).intercept { request ->
            val generationAtSend = refreshCoordinator.currentGeneration
            val originalCall = execute(request)
            if (originalCall.response.status != HttpStatusCode.Unauthorized) {
                return@intercept originalCall
            }

            val inRefresh = currentCoroutineContext()[RefreshGuard.Key] != null
            if (inRefresh) {
                return@intercept originalCall
            }

            if (!refreshCoordinator.refresh(generationAtSend)) {
                return@intercept originalCall
            }

            val refreshedToken = currentAccessToken()
            if (refreshedToken.isNotBlank()) {
                request.headers[HttpHeaders.Authorization] = "Bearer $refreshedToken"
            }

            execute(request)
        }
    }

    return client
}

private fun buildCsrfPlugin(
    headerName: String,
    provider: DevengCsrfTokenProvider
) = createClientPlugin("DevengCsrfPlugin") {
    onRequest { request, _ ->
        if (request.method.isMutating()) {
            provider.getToken()?.let { token ->
                request.headers[headerName] = token
            }
        }
    }
}

private fun HttpMethod.isMutating(): Boolean =
    this == HttpMethod.Post ||
        this == HttpMethod.Put ||
        this == HttpMethod.Patch ||
        this == HttpMethod.Delete
