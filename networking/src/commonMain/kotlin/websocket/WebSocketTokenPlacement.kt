package websocket

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders

private const val ACCESS_TOKEN_QUERY_KEY = "access_token"

internal enum class WebSocketTokenPlacement {
    AUTHORIZATION_HEADER,
    ACCESS_TOKEN_QUERY
}

internal expect val platformWebSocketTokenPlacement: WebSocketTokenPlacement

internal fun HttpRequestBuilder.applyWebSocketToken(token: String, tokenPlacement: WebSocketTokenPlacement) {
    if (token.isBlank()) return

    when (tokenPlacement) {
        WebSocketTokenPlacement.AUTHORIZATION_HEADER -> headers.append(HttpHeaders.Authorization, "Bearer $token")
        WebSocketTokenPlacement.ACCESS_TOKEN_QUERY -> url.parameters.append(ACCESS_TOKEN_QUERY_KEY, token)
    }
}
