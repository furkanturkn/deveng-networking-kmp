package websocket

// The browser WebSocket API cannot set request headers, so the token travels in the handshake URL.
internal actual val platformWebSocketTokenPlacement: WebSocketTokenPlacement = WebSocketTokenPlacement.ACCESS_TOKEN_QUERY
