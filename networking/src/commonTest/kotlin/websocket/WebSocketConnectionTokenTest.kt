package websocket

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import networking.exception_handling.ExceptionHandlerImpl

class WebSocketConnectionTokenTest {

    @AfterTest
    fun closeConnections() = runTest {
        WebSocketConnection.closeAll()
    }

    @Test
    fun start_withToken_sendsTheTokenWhereThePlatformExpectsIt() = runTest {
        val sentHandshake = CompletableDeferred<SentHandshake>()
        val connection = WebSocketConnection.getConnection(
            endpoint = ENDPOINT,
            client = buildHandshakeRecordingClient(sentHandshake),
            url = WEB_SOCKET_URL,
            exceptionHandler = ExceptionHandlerImpl
        )

        connection.start(
            token = ACCESS_TOKEN,
            onConnected = {},
            onMessageReceived = {},
            onError = {}
        )

        val handshake = withContext(Dispatchers.Default) {
            withTimeout(HANDSHAKE_TIMEOUT_MILLIS) { sentHandshake.await() }
        }

        when (platformWebSocketTokenPlacement) {
            WebSocketTokenPlacement.AUTHORIZATION_HEADER -> {
                assertEquals("Bearer $ACCESS_TOKEN", handshake.authorizationHeader)
                assertNull(handshake.accessTokenQuery)
            }

            WebSocketTokenPlacement.ACCESS_TOKEN_QUERY -> {
                assertEquals(ACCESS_TOKEN, handshake.accessTokenQuery)
                assertNull(handshake.authorizationHeader)
            }
        }
    }

    private fun buildHandshakeRecordingClient(sentHandshake: CompletableDeferred<SentHandshake>): HttpClient {
        val handshakeRecorder = createClientPlugin("HandshakeRecorder") {
            onRequest { request, _ ->
                sentHandshake.complete(
                    SentHandshake(
                        authorizationHeader = request.headers[HttpHeaders.Authorization],
                        accessTokenQuery = request.url.parameters[ACCESS_TOKEN_QUERY_KEY]
                    )
                )
            }
        }

        return HttpClient(MockEngine { respondError(HttpStatusCode.Unauthorized) }) {
            install(WebSockets)
            install(handshakeRecorder)
        }
    }

    private data class SentHandshake(
        val authorizationHeader: String?,
        val accessTokenQuery: String?
    )

    private companion object {
        const val ENDPOINT = "token-test"
        const val ACCESS_TOKEN = "jwt-value"
        const val ACCESS_TOKEN_QUERY_KEY = "access_token"
        const val WEB_SOCKET_URL = "wss://chat.example.test/ws"
        const val HANDSHAKE_TIMEOUT_MILLIS = 5_000L
    }
}
