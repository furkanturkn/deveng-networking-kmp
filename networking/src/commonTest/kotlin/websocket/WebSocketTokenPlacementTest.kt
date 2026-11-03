package websocket

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class WebSocketTokenPlacementTest {

    @Test
    fun applyWebSocketToken_withHeaderPlacement_addsTheBearerHeaderOnly() {
        val requestBuilder = buildRequest()

        requestBuilder.applyWebSocketToken(ACCESS_TOKEN, WebSocketTokenPlacement.AUTHORIZATION_HEADER)

        assertEquals("Bearer $ACCESS_TOKEN", requestBuilder.headers[HttpHeaders.Authorization])
        assertNull(requestBuilder.url.parameters[ACCESS_TOKEN_QUERY_KEY])
    }

    @Test
    fun applyWebSocketToken_withQueryPlacement_addsTheAccessTokenQueryOnly() {
        val requestBuilder = buildRequest()

        requestBuilder.applyWebSocketToken(ACCESS_TOKEN, WebSocketTokenPlacement.ACCESS_TOKEN_QUERY)

        assertEquals(ACCESS_TOKEN, requestBuilder.url.parameters[ACCESS_TOKEN_QUERY_KEY])
        assertFalse(requestBuilder.headers.contains(HttpHeaders.Authorization))
        assertEquals("wss://chat.example.test/ws?$ACCESS_TOKEN_QUERY_KEY=$ACCESS_TOKEN", requestBuilder.url.buildString())
    }

    @Test
    fun applyWebSocketToken_withBlankToken_addsNothing() {
        WebSocketTokenPlacement.entries.forEach { tokenPlacement ->
            val requestBuilder = buildRequest()

            requestBuilder.applyWebSocketToken(BLANK_TOKEN, tokenPlacement)

            assertFalse(requestBuilder.headers.contains(HttpHeaders.Authorization))
            assertNull(requestBuilder.url.parameters[ACCESS_TOKEN_QUERY_KEY])
        }
    }

    private fun buildRequest(): HttpRequestBuilder = HttpRequestBuilder().apply {
        url.takeFrom(WEB_SOCKET_URL)
    }

    private companion object {
        const val ACCESS_TOKEN = "jwt-value"
        const val BLANK_TOKEN = " "
        const val ACCESS_TOKEN_QUERY_KEY = "access_token"
        const val WEB_SOCKET_URL = "wss://chat.example.test/ws"
    }
}
