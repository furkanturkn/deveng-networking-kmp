package websocket

public sealed class ConnectionState {
        public data object Connected : ConnectionState()
        public data object Connecting : ConnectionState()
        public data object Disconnected : ConnectionState()
        public data class Error(val error: Throwable) : ConnectionState()
    }
