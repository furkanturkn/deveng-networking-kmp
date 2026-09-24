package networking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import networking.session.DevengSessionRefresher
import networking.session.GatedSessionRefresher

class DevengNetworkingModuleRefreshTest {

    @Test
    fun reinitialisingDuringARefreshDoesNotStartASecondOne() = runTest {
        val sessionRefresher = GatedSessionRefresher()
        val networkingModule = DevengNetworkingModule()
        networkingModule.initDevengNetworkingModule(REST_BASE_URL, configWith(sessionRefresher))

        val refreshBeforeReinitialisation = async { networkingModule.refreshSession() }
        sessionRefresher.awaitFirstCall()

        networkingModule.initDevengNetworkingModule(REST_BASE_URL, configWith(sessionRefresher))
        val refreshAfterReinitialisation = async { networkingModule.refreshSession() }
        testScheduler.runCurrent()
        sessionRefresher.release()

        assertTrue(refreshBeforeReinitialisation.await())
        assertTrue(refreshAfterReinitialisation.await())
        assertEquals(1, sessionRefresher.callCount)
    }

    @Test
    fun reinitialisingWithAnotherRefresherUsesTheNewOne() = runTest {
        val firstRefresher = GatedSessionRefresher()
        val secondRefresher = GatedSessionRefresher()
        val networkingModule = DevengNetworkingModule()
        networkingModule.initDevengNetworkingModule(REST_BASE_URL, configWith(firstRefresher))
        networkingModule.initDevengNetworkingModule(REST_BASE_URL, configWith(secondRefresher))
        secondRefresher.release()

        assertTrue(networkingModule.refreshSession())
        assertEquals(0, firstRefresher.callCount)
        assertEquals(1, secondRefresher.callCount)
    }

    @Test
    fun reinitialisingWithoutARefresherDisablesRefresh() = runTest {
        val sessionRefresher = GatedSessionRefresher()
        val networkingModule = DevengNetworkingModule()
        networkingModule.initDevengNetworkingModule(REST_BASE_URL, configWith(sessionRefresher))
        networkingModule.initDevengNetworkingModule(REST_BASE_URL, configWith(sessionRefresher = null))

        assertFalse(networkingModule.refreshSession())
        assertEquals(0, sessionRefresher.callCount)
    }

    private fun configWith(sessionRefresher: DevengSessionRefresher?) = DevengNetworkingConfig(
        loggingEnabled = false,
        token = EXPIRED_ACCESS_TOKEN,
        sessionRefresher = sessionRefresher
    )

    private companion object {
        const val REST_BASE_URL = "https://example.test/"
        const val EXPIRED_ACCESS_TOKEN = "expired-access-token"
    }
}
