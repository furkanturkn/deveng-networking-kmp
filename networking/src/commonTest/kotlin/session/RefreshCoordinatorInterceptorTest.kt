package networking.session

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.concurrent.Volatile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import networking.DevengNetworkingConfig
import networking.util.createHttpClient

class RefreshCoordinatorInterceptorTest {

    @Volatile
    private var currentAccessToken = EXPIRED_ACCESS_TOKEN

    // Real time, not the test scheduler: the mock engine answers on its own thread, and virtual time
    // would expire the coordinator's refresh timeout while the test waits for it.
    @Test
    fun a401ArrivingDuringARefreshIsReplayedWithTheRenewedToken() = runTest {
        withContext(Dispatchers.Default) { assertTheRefreshIsSharedWithThe401() }
    }

    private suspend fun assertTheRefreshIsSharedWithThe401() = coroutineScope {
        val sessionRefresher = GatedSessionRefresher(onRefresh = { currentAccessToken = RENEWED_ACCESS_TOKEN })
        val refreshCoordinator = RefreshCoordinator(sessionRefresher, REFRESH_TIMEOUT_MILLIS)
        val expiredRequestReceived = CompletableDeferred<Unit>()
        val mockEngine = MockEngine { request ->
            val isRenewedToken = request.headers[HttpHeaders.Authorization] == "Bearer $RENEWED_ACCESS_TOKEN"
            if (isRenewedToken) {
                respond(content = "", status = HttpStatusCode.OK)
            } else {
                expiredRequestReceived.complete(Unit)
                respond(content = "", status = HttpStatusCode.Unauthorized)
            }
        }
        val httpClient = createHttpClient(
            engine = mockEngine,
            config = DevengNetworkingConfig(loggingEnabled = false),
            currentAccessToken = { currentAccessToken },
            refreshCoordinator = refreshCoordinator
        )

        val proactiveRefresh = async { refreshCoordinator.refresh(refreshCoordinator.currentGeneration) }
        sessionRefresher.awaitFirstCall()
        val momentsResponse = async {
            httpClient.get(MOMENTS_URL) { header(HttpHeaders.Authorization, "Bearer $EXPIRED_ACCESS_TOKEN") }
        }
        expiredRequestReceived.await()
        sessionRefresher.release()

        assertTrue(proactiveRefresh.await())
        assertEquals(HttpStatusCode.OK, momentsResponse.await().status)
        assertEquals(1, sessionRefresher.callCount)
    }

    private companion object {
        const val MOMENTS_URL = "https://example.test/moments"
        const val EXPIRED_ACCESS_TOKEN = "expired-access-token"
        const val RENEWED_ACCESS_TOKEN = "renewed-access-token"
        const val REFRESH_TIMEOUT_MILLIS = 30_000L
    }
}
