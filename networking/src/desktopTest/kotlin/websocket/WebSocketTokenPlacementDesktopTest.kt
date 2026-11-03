package websocket

import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketTokenPlacementDesktopTest {

    @Test
    fun platformWebSocketTokenPlacement_onDesktop_usesTheAuthorizationHeader() {
        assertEquals(WebSocketTokenPlacement.AUTHORIZATION_HEADER, platformWebSocketTokenPlacement)
    }
}
