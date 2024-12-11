package networking

import error_handling.DevengException
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import networking.di.CoreModule
import networking.di.NetworkModule
import networking.localization.Locale

object DevengNetworkingModule {
    var BASE_URL: String = ""

    val client = NetworkModule.httpClient

    val exceptionHandler = CoreModule.exceptionHandler

    fun setBaseUrl(baseUrl: String) {
        BASE_URL = baseUrl
    }

    fun setLocale(locale: Locale) {
        exceptionHandler.locale = locale
    }

    suspend inline fun <reified T, reified R> sendRequest(
        endpoint: String,
        requestBody: T? = null,
        requestMethod: HttpMethod
    ): Result<R> {
        return try {
            val response: HttpResponse = client.request(
                urlString = "$BASE_URL$endpoint"
            ) {
                method = requestMethod
                if (requestBody != null) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
            }

            when {
                response.status.isSuccess() -> {
                    val responseBody: R = response.body()
                    Result.success(responseBody)
                }

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

