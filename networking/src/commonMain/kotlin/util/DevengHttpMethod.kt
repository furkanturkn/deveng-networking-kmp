package networking.util

import io.ktor.http.HttpMethod

public enum class DevengHttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    MULTIPART
}

public fun DevengHttpMethod.toKtorHttpMethod(): HttpMethod {
    return when (this) {
        DevengHttpMethod.GET -> HttpMethod.Get
        DevengHttpMethod.POST -> HttpMethod.Post
        DevengHttpMethod.PUT -> HttpMethod.Put
        DevengHttpMethod.DELETE -> HttpMethod.Delete
        DevengHttpMethod.MULTIPART -> HttpMethod.Post
    }
}