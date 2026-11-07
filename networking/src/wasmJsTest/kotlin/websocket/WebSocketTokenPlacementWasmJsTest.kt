package websocket

import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketTokenPlacementWasmJsTest {

    @Test
    fun platformWebSocketTokenPlacement_inTheBrowser_usesTheAccessTokenQuery() {
        assertEquals(WebSocketTokenPlacement.ACCESS_TOKEN_QUERY, platformWebSocketTokenPlacement)
    }
}
