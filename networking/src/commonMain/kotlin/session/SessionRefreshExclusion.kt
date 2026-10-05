package networking.session

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.util.AttributeKey

private val SessionRefreshExclusionKey = AttributeKey<Unit>("DevengSessionRefreshExclusion")

/**
 * Keeps a 401 of this request away from the session refresher. For requests that do not use the
 * session at all (a home screen widget's own token, for example): refreshing the session cannot fix
 * their 401, and the refresher failing would sign the user out.
 */
public fun HttpRequestBuilder.excludeFromSessionRefresh() {
    attributes.put(SessionRefreshExclusionKey, Unit)
}

internal fun HttpRequestBuilder.isExcludedFromSessionRefresh(): Boolean =
    attributes.contains(SessionRefreshExclusionKey)
