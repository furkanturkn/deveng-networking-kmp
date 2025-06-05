package networking

import di.NetworkModule
import error_handling.DevengException
import error_handling.DevengUiError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import networking.di.CoreModule
import networking.exception_handling.ExceptionHandler
import networking.localization.Locale
import networking.util.DevengHttpMethod
import networking.util.buildRequestUrl
import networking.util.logDebug
import networking.util.setupAllHeaders
import networking.util.setupQueryParameters
import networking.util.toKtorHttpMethod
import util.ErrorResponse
import websocket.WebSocketConnection

public data class DevengNetworkingConfig(
    val loggingEnabled: Boolean = true,
    val requestTimeoutMillis: Long = 60_000L,
    val connectTimeoutMillis: Long = 10_000L,
    val socketTimeoutMillis: Long = 60_000L,
    val token: String = "",
    val locale: Locale? = null,
    val customHeaders: Map<String, String> = emptyMap(),
    val socketBaseUrl: String = ""
)

public object DevengNetworkingModule {
    public var client: HttpClient? = null
    public var exceptionHandler: ExceptionHandler? = null
    public var sharedJson: Json? = null

    private var config: DevengNetworkingConfig? = null
    private var restBaseUrl: String = ""

    // Internal getters for HTTP client configuration
    internal val loggingEnabled: Boolean get() = config?.loggingEnabled ?: true
    internal val requestTimeoutMillis: Long get() = config?.requestTimeoutMillis ?: 10_000L
    internal val connectTimeoutMillis: Long get() = config?.connectTimeoutMillis ?: 10_000L
    internal val socketTimeoutMillis: Long get() = config?.socketTimeoutMillis ?: 10_000L

    public fun initDevengNetworkingModule(
        restBaseUrl: String,
        config: DevengNetworkingConfig
    ) {
        this.config = config
        this.restBaseUrl = restBaseUrl

        if (config.locale != null) {
            exceptionHandler?.locale = config.locale
        }

        client = NetworkModule.httpClient
        exceptionHandler = CoreModule.exceptionHandler

        sharedJson = CoreModule.sharedJson
    }

    // Internal getters for request handling
    public fun getRestBaseUrl(): String = restBaseUrl
    public fun getSocketBaseUrl(): String = config?.socketBaseUrl ?: ""
    public fun getToken(): String = config?.token ?: ""
    public fun getCustomHeaders(): Map<String, String> = config?.customHeaders ?: emptyMap()

    public fun setToken(newToken: String) {
        config = config?.copy(token = newToken)
    }

    public suspend inline fun <reified T, reified R> sendRequest(
        endpoint: String,
        requestBody: T? = null,
        requestMethod: DevengHttpMethod,
        queryParameters: Map<String, String>? = null,
        pathParameters: Map<String, String>? = null
    ): R {
        return try {
            if (client == null) {
                throw (IllegalStateException("Client is not initialized"))
            }

            val response: HttpResponse = client!!.request(
                urlString = buildRequestUrl(endpoint, pathParameters)
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAllHeaders()

                setupQueryParameters(queryParameters)

                if (requestBody != null) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
            }

            when {
                response.status.isSuccess() -> response.body() as R

                else -> {
                    var errorResponse: ErrorResponse? = null
                    try {
                        errorResponse = sharedJson?.decodeFromString<ErrorResponse>(response.body())
                    } catch (e: Exception) {
                        logDebug(message = "Cannot decode error response")
                    }

                    val error = exceptionHandler?.handleHttpException(
                        errorMessage = errorResponse?.message,
                        status = response.status
                    )
                    throw DevengException(error ?: DevengUiError.UnknownError("Unknown error"))
                }
            }
        } catch (e: Exception) {
            if (e is DevengException) {
                throw e
            } else {
                val error = exceptionHandler?.handleNetworkException(e)
                logDebug(message = e.message)
                logDebug(message = e.cause)
                throw DevengException(error ?: DevengUiError.UnknownError("Unknown error"))
            }
        }
    }

    public suspend fun <T : Any, R : Any> sendRequest(
        endpoint: String,
        requestBody: T? = null,
        requestMethod: DevengHttpMethod,
        queryParameters: Map<String, String>? = null,
        pathParameters: Map<String, String>? = null,
        requestSerializer: KSerializer<T>? = null,
        responseSerializer: KSerializer<R>
    ): R {
        if (client == null) {
            throw (IllegalStateException("Client is not initialized"))
        }

        return try {
            val response: HttpResponse = client!!.request(
                urlString = buildRequestUrl(endpoint, pathParameters)
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAllHeaders()

                setupQueryParameters(queryParameters)

                if (requestBody != null && requestSerializer != null) {
                    contentType(ContentType.Application.Json)
                    setBody(sharedJson?.encodeToString(requestSerializer, requestBody))
                }
            }

            when {
                response.status.isSuccess() -> sharedJson!!.decodeFromString(
                    responseSerializer,
                    response.bodyAsText()
                )

                else -> {
                    var errorResponse: ErrorResponse? = null
                    try {
                        errorResponse =
                            sharedJson?.decodeFromString(
                                ErrorResponse.serializer(),
                                response.bodyAsText()
                            )
                    } catch (e: Exception) {
                        logDebug(message = "Cannot decode error response")
                    }

                    val error = exceptionHandler?.handleHttpException(
                        errorMessage = errorResponse?.message,
                        status = response.status
                    )
                    throw DevengException(error ?: DevengUiError.UnknownError("Unknown error"))
                }
            }
        } catch (e: Exception) {
            if (e is DevengException) {
                throw e
            } else {
                val error = exceptionHandler?.handleNetworkException(e)
                logDebug(message = e.message.toString())
                throw DevengException(error ?: DevengUiError.UnknownError("Unknown error"))
            }
        }
    }

    public suspend inline fun <reified T> sendRequestForHttpResponse(
        endpoint: String,
        requestBody: T? = null,
        requestMethod: DevengHttpMethod,
        queryParameters: Map<String, String>? = null,
        pathParameters: Map<String, String>? = null
    ): HttpResponse {
        if (client == null) {
            throw (IllegalStateException("Client is not initialized"))
        }

        return try {
            val response: HttpResponse = client!!.request(
                urlString = buildRequestUrl(endpoint, pathParameters)
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAllHeaders()

                setupQueryParameters(queryParameters)

                if (requestBody != null) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
            }

            when {
                response.status.isSuccess() -> response

                else -> {
                    var errorResponse: ErrorResponse? = null
                    try {
                        errorResponse = sharedJson?.decodeFromString<ErrorResponse>(response.body())
                    } catch (e: Exception) {
                        logDebug(message = "Cannot decode error response")
                    }

                    val error = exceptionHandler?.handleHttpException(
                        errorMessage = errorResponse?.message,
                        status = response.status
                    )
                    throw DevengException(error ?: DevengUiError.UnknownError("Unknown error"))
                }
            }
        } catch (e: Exception) {
            if (e is DevengException) {
                throw e
            } else {
                val error = exceptionHandler?.handleNetworkException(e)
                logDebug(message = e.message)
                logDebug(message = e.cause)
                throw DevengException(error ?: DevengUiError.UnknownError("Unknown error"))
            }
        }
    }

    public suspend fun connectToWebSocket(
        endpoint: String,
        onConnected: suspend WebSocketConnection.() -> Unit,
        onMessageReceived: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onClose: (() -> Unit)? = null
    ): WebSocketConnection {
        if (client == null) {
            throw (IllegalStateException("Client is not initialized"))
        }

        if (exceptionHandler == null) {
            throw (IllegalStateException("Exception handler is not initialized"))
        }

        val fullUrl = "${getSocketBaseUrl()}$endpoint"
        val connection = WebSocketConnection.getConnection(
            endpoint = endpoint,
            client = client!!,
            url = fullUrl,
            exceptionHandler = exceptionHandler!!
        )
        connection.start(
            onConnected = onConnected,
            onMessageReceived = onMessageReceived,
            onError = onError,
            onClose = onClose
        )
        return connection
    }

    public suspend fun closeWebSocketConnection(endpoint: String) {
        WebSocketConnection.closeConnection(endpoint)
    }

    public suspend fun closeAllWebSocketConnections() {
        WebSocketConnection.closeAll()
    }
}
