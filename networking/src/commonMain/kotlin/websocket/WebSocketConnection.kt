package websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import networking.exception_handling.ExceptionHandler

public class WebSocketConnection private constructor(
    private val client: HttpClient,
    private val url: String,
    private val exceptionHandler: ExceptionHandler
) {
    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    public val connectionState: StateFlow<ConnectionState> = _connectionState

    public companion object Manager {
        private var maxConnections: Int = 5
        private val connections = LinkedHashMap<String, WebSocketConnection>()

        public fun setMaxConnections(limit: Int) {
            require(limit > 0) { "Connection limit must be greater than 0" }
            maxConnections = limit

            while (connections.size > maxConnections) {
                val oldestEndpoint = connections.keys.first()
                closeConnection(oldestEndpoint)
                println("New connection limit applied. Closing oldest connection: $oldestEndpoint")
            }
        }

        public fun getMaxConnections(): Int = maxConnections

        public fun getConnection(
            endpoint: String,
            client: HttpClient,
            url: String,
            exceptionHandler: ExceptionHandler
        ): WebSocketConnection {
            connections[endpoint]?.let { return it }

            if (connections.size >= maxConnections) {
                val oldestEndpoint = connections.keys.first()
                closeConnection(oldestEndpoint)
                println("Connection limit reached. Closing oldest connection: $oldestEndpoint")
            }

            return WebSocketConnection(client, url, exceptionHandler).also {
                connections[endpoint] = it
            }
        }

        public fun closeAll() {
            connections.values.forEach { it.closeSession() }
            connections.clear()
        }

        public fun closeConnection(endpoint: String) {
            connections.remove(endpoint)?.closeSession()
        }

        public fun getActiveConnections(): Set<String> = connections.keys

        public fun getConnectionCount(): Int = connections.size
    }

    public fun start(
        onConnected: suspend WebSocketConnection.() -> Unit,
        onMessageReceived: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onClose: (() -> Unit)? = null
    ) {
        connectionJob?.cancel()
        
        _connectionState.value = ConnectionState.Connecting

        connectionJob = scope.launch {
            try {
                client.webSocket(urlString = url) {
                    session = this
                    try {
                        _connectionState.value = ConnectionState.Connected
                        onConnected(this@WebSocketConnection)

                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    try {
                                        onMessageReceived(frame.readText())
                                    } catch (e: Exception) {
                                        val handled = exceptionHandler.handleNetworkException(e)
                                        onError(handled)
                                    }
                                }
                                is Frame.Close -> {
                                    onClose?.invoke()
                                    closeSession()
                                    break
                                }
                                else -> {
                                    println("Unsupported frame type: $frame")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        val handled = exceptionHandler.handleNetworkException(e)
                        _connectionState.value = ConnectionState.Error(handled)
                        onError(handled)
                        closeSession()
                    }
                }
            } catch (e: Exception) {
                val handled = exceptionHandler.handleNetworkException(e)
                _connectionState.value = ConnectionState.Error(handled)
                onError(handled)
                closeSession()
            }
        }
    }

    public suspend fun sendMessage(message: String) {
        when (connectionState.value) {
            is ConnectionState.Connected -> {
                session?.send(Frame.Text(message))
                    ?: throw IllegalStateException("WebSocket session is not open")
            }
            else -> throw IllegalStateException("Cannot send message when not connected. Current state: ${connectionState.value}")
        }
    }

    public fun closeSession() {
        connectionJob?.cancel()
        scope.launch {
            try {
                session?.close()
            } catch (e: Exception) {
                println("Error closing WebSocket session: $e")
            } finally {
                cleanup()
            }
        }
    }

    private fun cleanup() {
        connectionJob?.cancel()
        connectionJob = null
        session = null
        _connectionState.value = ConnectionState.Disconnected
    }
}