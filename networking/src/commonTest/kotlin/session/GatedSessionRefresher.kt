package networking.session

import kotlin.concurrent.Volatile
import kotlinx.coroutines.CompletableDeferred

/**
 * A refresher that holds every call until [release], so a test can act while a refresh is in flight.
 */
internal class GatedSessionRefresher(
    private val onRefresh: () -> Unit = {}
) : DevengSessionRefresher {

    private val firstCallStarted = CompletableDeferred<Unit>()
    private val releaseGate = CompletableDeferred<Unit>()

    @Volatile
    var callCount: Int = 0
        private set

    override suspend fun refresh(): Boolean {
        callCount += 1
        firstCallStarted.complete(Unit)
        releaseGate.await()
        onRefresh()
        return true
    }

    suspend fun awaitFirstCall() {
        firstCallStarted.await()
    }

    fun release() {
        releaseGate.complete(Unit)
    }
}
