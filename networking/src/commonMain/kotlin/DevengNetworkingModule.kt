package networking

import di.NetworkModule
import error_handling.DevengException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import networking.di.CoreModule
import networking.exception_handling.ExceptionHandler
import networking.localization.Locale
import networking.util.*

public object DevengNetworkingModule {
    public var baseUrl: String = ""

    public var token: String =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1laWQiOiIxIiwidW5pcXVlX25hbWUiOiJhZG1pbiIsInJvbGUiOiJBZG1pbiIsIm5iZiI6MTczNDE3MjQ1NSwiZXhwIjoxNzM0MTc2MDU1LCJpYXQiOjE3MzQxNzI0NTV9.HrDahda-NIvAcTq8urLpTlX4P6-xCj75fxlRC3LZA30"

    public val client: HttpClient = NetworkModule.httpClient

    public val exceptionHandler: ExceptionHandler = CoreModule.exceptionHandler

    public fun setApiBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    public fun setBearerToken(token: String) {
        this.token = token
    }

    public fun setLocale(locale: Locale) {
        exceptionHandler.locale = locale
    }

    public suspend inline fun <reified T, reified R> sendRequest(
        endpoint: String,
        requestBody: T? = null,
        requestMethod: DevengHttpMethod,
        queryParameters: Map<String, String>? = null,
        pathParameters: Map<String, String>? = null
    ): R {
        return try {
            val resolvedEndpoint = endpoint.addPathParameters(pathParameters = pathParameters)

            val response: HttpResponse = client.request(
                urlString = "$baseUrl$resolvedEndpoint"
            ) {
                method = requestMethod.toKtorHttpMethod()

                setupAuthorizationHeader(
                    token = token
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
                response.status.isSuccess() -> response.body() as R

                else -> {
                    val error = exceptionHandler.handleHttpException(response.status)
                    throw DevengException(error)
                }
            }
        } catch (e: Exception) {
            println(e)
            val error = exceptionHandler.handleNetworkException(e)
            throw DevengException(error)
        }
    }


}
