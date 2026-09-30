package networking.session

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import networking.DevengNetworkingConfig
import networking.util.createHttpClient

class SessionRefreshExclusionTest {

    @Test
    fun a401OfAnExcludedRequestNeverRefreshesTheSession() = runTest {
        var refreshCount = 0
        val refreshCoordinator = RefreshCoordinator(
            refresher = {
                refreshCount++
                true
            },
            refreshTimeoutMillis = REFRESH_TIMEOUT_MILLIS
        )
        val httpClient = createHttpClient(
            engine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) },
            config = DevengNetworkingConfig(loggingEnabled = false),
            currentAccessToken = { ACCESS_TOKEN },
            refreshCoordinator = refreshCoordinator
        )

        val widgetResponse = httpClient.get(WIDGET_URL) { excludeFromSessionRefresh() }

        assertEquals(HttpStatusCode.Unauthorized, widgetResponse.status)
        assertEquals(0, refreshCount)
    }

    @Test
    fun a401OfARegularRequestStillRefreshesTheSession() = runTest {
        var refreshCount = 0
        val refreshCoordinator = RefreshCoordinator(
            refresher = {
                refreshCount++
                false
            },
            refreshTimeoutMillis = REFRESH_TIMEOUT_MILLIS
        )
        val httpClient = createHttpClient(
            engine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) },
            config = DevengNetworkingConfig(loggingEnabled = false),
            currentAccessToken = { ACCESS_TOKEN },
            refreshCoordinator = refreshCoordinator
        )

        httpClient.get(WIDGET_URL)

        assertEquals(1, refreshCount)
    }

    private companion object {
        const val WIDGET_URL = "https://example.test/widgets/home-screen/latest"
        const val ACCESS_TOKEN = "access-token"
        const val REFRESH_TIMEOUT_MILLIS = 30_000L
    }
}
