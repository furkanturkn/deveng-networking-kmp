package di

import io.ktor.client.HttpClient

internal expect object NetworkModule {
    val httpClient: HttpClient
}