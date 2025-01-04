package websocket

public sealed class ConnectionState {
        internal data object Connected : ConnectionState()
        internal data object Connecting : ConnectionState()
        internal data object Disconnected : ConnectionState()
        internal data class Error(val error: Throwable) : ConnectionState()
    }