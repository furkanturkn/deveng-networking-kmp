package networking.csrf

/**
 * Supplies a CSRF token for mutating requests.
 *
 * When wired through [networking.DevengNetworkingConfig.csrfTokenProvider], the client
 * calls [getToken] on every POST/PUT/PATCH/DELETE and, if the result is non-null,
 * attaches it under [networking.DevengNetworkingConfig.csrfHeaderName].
 */
public fun interface DevengCsrfTokenProvider {
    public suspend fun getToken(): String?
}
