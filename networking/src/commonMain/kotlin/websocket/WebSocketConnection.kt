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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import networking.exception_handling.ExceptionHandler
import kotlin.coroutines.cancellation.CancellationException

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

    private val mutex = Mutex()
    private var isClosing = false

    public companion object Manager {
        private var maxConnections: Int = 5
        private val connections = LinkedHashMap<String, WebSocketConnection>()

        public suspend fun setMaxConnections(limit: Int) {
            require(limit > 0) { "Connection limit must be greater than 0" }
            maxConnections = limit

            while (connections.size > maxConnections) {
                val oldestEndpoint = connections.keys.first()
                closeConnection(oldestEndpoint)
                println("New connection limit applied. Closing oldest connection: $oldestEndpoint")
            }
        }

        public fun getMaxConnections(): Int = maxConnections

        public suspend fun getConnection(
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

        public suspend fun closeAll() {
            connections.values.forEach { it.closeSession() }
            connections.clear()
        }

        public suspend fun closeConnection(endpoint: String) {
            connections.remove(endpoint)?.closeSession()
        }

        public fun getActiveConnections(): Set<String> = connections.keys

        public fun getConnectionCount(): Int = connections.size
    }

    public suspend fun start(
        onConnected: suspend WebSocketConnection.() -> Unit,
        onMessageReceived: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onClose: (() -> Unit)? = null
    ) {
        mutex.withLock {
            if (isClosing) {
                println("Connection is currently closing, waiting for cleanup...")
                return
            }

            if (_connectionState.value is ConnectionState.Connected) {
                println("WebSocket is already connected to: $url")
                return
            }

            if (_connectionState.value is ConnectionState.Connecting) {
                println("WebSocket is already connecting to: $url")
                return
            }

            // Ensure clean state before starting
            cleanupInternal()
            
            _connectionState.value = ConnectionState.Connecting
            println("Starting new WebSocket connection to: $url")

            connectionJob = scope.launch {
                try {
                    client.webSocket(urlString = url) {
                        session = this
                        try {
                            _connectionState.value = ConnectionState.Connected
                            println("WebSocket connected successfully to: $url")
                            onConnected(this@WebSocketConnection)

                            for (frame in incoming) {
                                if (_connectionState.value !is ConnectionState.Connected || isClosing) {
                                    break
                                }
                                when (frame) {
                                    is Frame.Text -> {
                                        try {
                                            onMessageReceived(frame.readText())
                                        } catch (e: Exception) {
                                            println("Error processing message: ${e.message}")
                                            val handled = exceptionHandler.handleNetworkException(e)
                                            onError(handled)
                                        }
                                    }
                                    is Frame.Close -> {
                                        println("WebSocket received close frame")
                                        onClose?.invoke()
                                        closeSessionInternal()
                                        break
                                    }
                                    else -> {
                                        println("Unsupported frame type: $frame")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("WebSocket session error: ${e.message}")
                            val handled = exceptionHandler.handleNetworkException(e)
                            _connectionState.value = ConnectionState.Error(handled)
                            onError(handled)
                            closeSessionInternal()
                        }
                    }
                } catch (e: CancellationException) {
                    println("WebSocket connection cancelled for: $url")
                    _connectionState.value = ConnectionState.Disconnected
                    closeSessionInternal()
                } catch (e: Exception) {
                    println("WebSocket connection error: ${e.message}")
                    val handled = exceptionHandler.handleNetworkException(e)
                    _connectionState.value = ConnectionState.Error(handled)
                    onError(handled)
                    closeSessionInternal()
                }
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

    public suspend fun closeSession() {
        mutex.withLock {
            closeSessionInternal()
        }
    }

    private suspend fun closeSessionInternal() {
        if (isClosing) return
        isClosing = true
        
        try {
            connectionJob?.cancel()
            session?.close()
        } catch (e: Exception) {
            println("Error closing WebSocket session: ${e.message}")
        } finally {
            cleanupInternal()
            isClosing = false
        }
    }

    private fun cleanupInternal() {
        connectionJob?.cancel()
        connectionJob = null
        session = null
        _connectionState.value = ConnectionState.Disconnected
        println("WebSocket connection cleaned up for: $url")
    }
}