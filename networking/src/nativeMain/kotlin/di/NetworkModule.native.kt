package networking.data.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import networking.util.createHttpClient

actual object NetworkModule {
    actual val httpClient: HttpClient by lazy {
        createHttpClient(Darwin.create())
    }
}