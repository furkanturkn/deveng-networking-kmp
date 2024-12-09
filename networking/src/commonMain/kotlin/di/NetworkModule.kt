package networking.di

import io.ktor.client.HttpClient

expect object NetworkModule {
    val httpClient: HttpClient
}