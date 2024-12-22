package websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.launch
import networking.exception_handling.ExceptionHandler

public class WebSocketConnection(
    private val client: HttpClient,
    private val url: String,
    private val exceptionHandler: ExceptionHandler
) {
    private var session: DefaultClientWebSocketSession? = null

    public fun start(
        onConnected: suspend WebSocketConnection.() -> Unit,
        onMessageReceived: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onClose: (() -> Unit)? = null
    ) {
        try {
            client.launch {
                client.webSocket(urlString = url) {
                    session = this
                    try {
                        onConnected(this@WebSocketConnection)

                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> onMessageReceived(frame.readText())
                                is Frame.Close -> onClose?.invoke()
                                else -> {}
                            }
                        }
                    } catch (e: Exception) {
                        onError(exceptionHandler.handleNetworkException(e))
                    }
                }
            }
        } catch (e: Exception) {
            onError(exceptionHandler.handleNetworkException(e))
        }
    }

    public suspend fun sendMessage(message: String) {
        session?.send(Frame.Text(message))
            ?: throw IllegalStateException("WebSocket session is not open")
    }

    public suspend fun close() {
        session?.close()
        session = null
    }
}