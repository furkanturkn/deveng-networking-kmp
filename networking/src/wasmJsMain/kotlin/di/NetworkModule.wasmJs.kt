package networking.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import networking.util.createHttpClient

actual object NetworkModule {
    actual val httpClient: HttpClient by lazy {
        createHttpClient(Js.create())
    }
}