package di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import networking.util.createHttpClient

internal actual object NetworkModule {
    actual val httpClient: HttpClient by lazy {
        createHttpClient(OkHttp.create())
    }
}