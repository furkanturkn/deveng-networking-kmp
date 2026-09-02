package networking.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import networking.DevengNetworkingConfig
import networking.session.DevengSessionRefresher

private const val EXPIRED_TOKEN = "expired-access-token"
private const val REFRESHED_TOKEN = "refreshed-access-token"
private const val PROTECTED_URL = "https://example.com/protected"
private const val REFRESH_URL = "https://example.com/auth/refresh"
private const val EMPTY_BODY = ""
private const val SUCCESS_BODY = "ok"
private const val CONCURRENT_REQUEST_COUNT = 5
private const val REFRESH_LATENCY_MILLIS = 10L
private const val REFRESH_TIMEOUT_MILLIS = 100L
private const val TIMEOUT_OVERSHOOT_FACTOR = 10L
private const val DEFAULT_REFRESH_TIMEOUT_MILLIS = 30_000L

class RefreshInterceptorTest {

    @Test
    fun retriesWithRefreshedTokenAfterUnauthorized() = runTest {
        val accessToken = MutableAccessToken()
        val sentAuthorizationHeaders = mutableListOf<String?>()
        var refreshCount = 0

        val engine = MockEngine { request ->
            sentAuthorizationHeaders += request.headers[HttpHeaders.Authorization]
            respondForToken(request.headers[HttpHeaders.Authorization])
        }
        val client = buildClient(engine, accessToken) {
            refreshCount++
            accessToken.value = REFRESHED_TOKEN
            true
        }

        val response = client.get(PROTECTED_URL) {
            header(HttpHeaders.Authorization, bearer(EXPIRED_TOKEN))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, refreshCount)
        assertEquals(
            listOf<String?>(bearer(EXPIRED_TOKEN), bearer(REFRESHED_TOKEN)),
            sentAuthorizationHeaders.toList()
        )
    }

    @Test
    fun replacesAuthorizationHeaderInsteadOfAppendingOnRetry() = runTest {
        val accessToken = MutableAccessToken()
        val authorizationHeaderCounts = mutableListOf<Int>()

        val engine = MockEngine { request ->
            authorizationHeaderCounts += request.headers.getAll(HttpHeaders.Authorization)?.size ?: 0
            respondForToken(request.headers[HttpHeaders.Authorization])
        }
        val client = buildClient(engine, accessToken) {
            accessToken.value = REFRESHED_TOKEN
            true
        }

        client.get(PROTECTED_URL) {
            header(HttpHeaders.Authorization, bearer(EXPIRED_TOKEN))
        }

        assertEquals(listOf(1, 1), authorizationHeaderCounts)
    }

    @Test
    fun propagatesUnauthorizedWhenRefreshFails() = runTest {
        var requestCount = 0
        var refreshCount = 0

        val engine = MockEngine {
            requestCount++
            respond(EMPTY_BODY, HttpStatusCode.Unauthorized)
        }
        val client = buildClient(engine, MutableAccessToken()) {
            refreshCount++
            false
        }

        val response = client.get(PROTECTED_URL)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, refreshCount)
        assertEquals(1, requestCount, "A failed refresh must not replay the original request.")
    }

    @Test
    fun concurrentUnauthorizedResponsesTriggerASingleRefresh() = runTest {
        val accessToken = MutableAccessToken()
        var refreshCount = 0

        val engine = MockEngine { request ->
            respondForToken(request.headers[HttpHeaders.Authorization])
        }
        val client = buildClient(engine, accessToken) {
            refreshCount++
            delay(REFRESH_LATENCY_MILLIS)
            accessToken.value = REFRESHED_TOKEN
            true
        }

        val responses = coroutineScope {
            List(CONCURRENT_REQUEST_COUNT) {
                async {
                    client.get(PROTECTED_URL) {
                        header(HttpHeaders.Authorization, bearer(EXPIRED_TOKEN))
                    }
                }
            }.awaitAll()
        }

        assertTrue(responses.all { it.status == HttpStatusCode.OK })
        assertEquals(1, refreshCount, "Concurrent 401s must be deduplicated into one refresh.")
    }

    @Test
    fun requestsIssuedByTheRefreshItselfDoNotRecurse() = runTest {
        var refreshCount = 0
        lateinit var client: HttpClient

        val engine = MockEngine { respond(EMPTY_BODY, HttpStatusCode.Unauthorized) }
        client = buildClient(engine, MutableAccessToken()) {
            refreshCount++
            // A refresh endpoint answering 401 must not re-enter the interceptor.
            client.get(REFRESH_URL)
            false
        }

        val response = client.get(PROTECTED_URL)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, refreshCount)
    }

    @Test
    fun hangingRefreshTimesOutInsteadOfBlockingTheRequest() = runTest {
        var requestCount = 0
        var refreshRanToCompletion = false

        val engine = MockEngine {
            requestCount++
            respond(EMPTY_BODY, HttpStatusCode.Unauthorized)
        }
        val client = buildClient(
            engine = engine,
            accessToken = MutableAccessToken(),
            refreshTimeoutMillis = REFRESH_TIMEOUT_MILLIS
        ) {
            delay(REFRESH_TIMEOUT_MILLIS * TIMEOUT_OVERSHOOT_FACTOR)
            refreshRanToCompletion = true
            true
        }

        val response = client.get(PROTECTED_URL)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertFalse(refreshRanToCompletion, "The timeout must cancel the stalled refresh.")
        assertEquals(1, requestCount, "A timed-out refresh must not replay the original request.")
    }

    @Test
    fun unauthorizedPropagatesWhenNoRefresherIsConfigured() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            respond(EMPTY_BODY, HttpStatusCode.Unauthorized)
        }
        val client = createHttpClient(
            engine = engine,
            config = DevengNetworkingConfig(loggingEnabled = false),
            currentAccessToken = { EXPIRED_TOKEN }
        )

        val response = client.get(PROTECTED_URL)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, requestCount)
    }
}

private class MutableAccessToken(var value: String = EXPIRED_TOKEN)

private fun bearer(token: String) = "Bearer $token"

private fun MockRequestHandleScope.respondForToken(authorizationHeader: String?): HttpResponseData =
    if (authorizationHeader == bearer(REFRESHED_TOKEN)) {
        respond(SUCCESS_BODY, HttpStatusCode.OK)
    } else {
        respond(EMPTY_BODY, HttpStatusCode.Unauthorized)
    }

private fun buildClient(
    engine: MockEngine,
    accessToken: MutableAccessToken,
    refreshTimeoutMillis: Long = DEFAULT_REFRESH_TIMEOUT_MILLIS,
    refresher: DevengSessionRefresher
): HttpClient = createHttpClient(
    engine = engine,
    config = DevengNetworkingConfig(
        loggingEnabled = false,
        sessionRefresher = refresher,
        refreshTimeoutMillis = refreshTimeoutMillis
    ),
    currentAccessToken = { accessToken.value }
)
