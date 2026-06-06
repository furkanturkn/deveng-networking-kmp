package networking.session

import kotlin.coroutines.CoroutineContext

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
