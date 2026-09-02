package networking.session

import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pluggable session-refresh hook called by [networking.DevengNetworkingModule] when a
 * request comes back with HTTP 401.
 *
 * Return `true` if the refresh succeeded and the original request should be retried.
 * Return `false` to let the 401 propagate as
 * [error_handling.DevengUiError.UnauthorizedError].
 *
 * Concurrent 401s are deduplicated by a single-flight [kotlinx.coroutines.sync.Mutex]
 * inside the client, so [refresh] is called at most once at a time. An internal
 * coroutine-context marker also prevents recursion when a `refresh` implementation
 * itself issues HTTP calls through the same client.
 */
public fun interface DevengSessionRefresher {
    public suspend fun refresh(): Boolean
}

internal object RefreshGuard : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    internal object Key : CoroutineContext.Key<RefreshGuard>
}

/**
 * Serializes [DevengSessionRefresher] invocations so that a burst of 401s triggers a single refresh.
 *
 * [generation] is bumped once per successful refresh. A caller records the generation it saw before
 * sending its request; if the value moved while the request was in flight, someone else already
 * installed a fresh token and the caller can retry without refreshing again.
 */
internal class RefreshCoordinator(
    private val refresher: DevengSessionRefresher,
    private val refreshTimeoutMillis: Long
) {
    private val mutex = Mutex()

    @Volatile
    private var generation = 0L

    internal val currentGeneration: Long get() = generation

    internal suspend fun refresh(generationAtSend: Long): Boolean = mutex.withLock {
        if (generation != generationAtSend) {
            return@withLock true
        }

        val succeeded = runGuarded()
        if (succeeded) {
            generation++
        }
        succeeded
    }

    /**
     * [RefreshGuard] marks the coroutine so the 401 interceptor skips requests issued by the refresh
     * itself, which would otherwise recurse. The timeout caps how long the mutex is held: without it a
     * hanging refresh endpoint stalls every other request that hits a 401.
     */
    private suspend fun runGuarded(): Boolean = withContext(RefreshGuard) {
        if (refreshTimeoutMillis <= 0) {
            refresher.refresh()
        } else {
            withTimeoutOrNull(refreshTimeoutMillis) { refresher.refresh() } ?: false
        }
    }
}
