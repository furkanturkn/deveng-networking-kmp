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
import networking.util.*
import util.ErrorResponse
import websocket.WebSocketConnection

public object DevengNetworkingModule {
    public var restBaseUrl: String = ""
    public var socketBaseUrl: String = ""

    public var loggingEnabled: Boolean = true

    public var token: String = ""

    public var client: HttpClient? = null

    public var exceptionHandler: ExceptionHandler? = null

    public fun initDevengNetworkingModule(
        restBaseUrl: String,
        socketBaseUrl: String,
        loggingEnabled: Boolean = true,
        token: String = "",
        locale: Locale? = null
    ) {
        setApiBaseUrl(restBaseUrl)
        setWebSocketBaseUrl(socketBaseUrl)
        setLoggingState(loggingEnabled)
        setBearerToken(token)
        if (locale != null) {
            setLocale(locale)
        }

        client = NetworkModule.httpClient
        exceptionHandler = CoreModule.exceptionHandler
    }

    private fun setLoggingState(enabled: Boolean) {
        this.loggingEnabled = enabled
    }

    public fun setApiBaseUrl(url: String) {
        this.restBaseUrl = url
    }

    public fun setWebSocketBaseUrl(url: String) {
        this.socketBaseUrl = url
    }

    public fun setBearerToken(token: String) {
        this.token = token
    }

    public fun setLocale(locale: Locale) {
        exceptionHandler?.locale = locale
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


            val resolvedEndpoint = endpoint.addPathParameters(pathParameters = pathParameters)

            val response: HttpResponse = client!!.request(
                urlString = "$restBaseUrl$resolvedEndpoint"
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAuthorizationHeader(
                    token = token
                )

                if (exceptionHandler?.locale != null) {
                    setupLocaleHeader(
                        locale = exceptionHandler?.locale.toString()
                    )
                }

                url {
                    addQueryParameters(queryParameters = queryParameters)
                }

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
                        errorResponse = Json.decodeFromString<ErrorResponse>(response.body())
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
            val resolvedEndpoint = endpoint.addPathParameters(pathParameters = pathParameters)

            val response: HttpResponse = client!!.request(
                urlString = "$restBaseUrl$resolvedEndpoint"
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAuthorizationHeader(
                    token = token
                )

                if (exceptionHandler?.locale != null) {
                    setupLocaleHeader(
                        locale = exceptionHandler?.locale.toString()
                    )
                }

                url {
                    addQueryParameters(queryParameters = queryParameters)
                }

                if (requestBody != null && requestSerializer != null) {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(requestSerializer, requestBody))
                }
            }

            when {
                response.status.isSuccess() -> Json.decodeFromString(
                    responseSerializer,
                    response.bodyAsText()
                )

                else -> {
                    var errorResponse: ErrorResponse? = null
                    try {
                        errorResponse =
                            Json.decodeFromString(ErrorResponse.serializer(), response.bodyAsText())
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
            val resolvedEndpoint = endpoint.addPathParameters(pathParameters = pathParameters)

            val response: HttpResponse = client!!.request(
                urlString = "$restBaseUrl$resolvedEndpoint"
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAuthorizationHeader(
                    token = token
                )

                setupLocaleHeader(
                    locale = exceptionHandler?.locale.toString()
                )

                url {
                    addQueryParameters(queryParameters = queryParameters)
                }

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
                        errorResponse = Json.decodeFromString<ErrorResponse>(response.body())
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

        val fullUrl = "$socketBaseUrl$endpoint"
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
