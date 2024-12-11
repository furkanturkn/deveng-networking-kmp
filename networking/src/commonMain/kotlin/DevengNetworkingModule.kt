package networking

import error_handling.DevengException
import error_handling.DevengUiError
import error_handling.ErrorKey
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import networking.di.NetworkModule
import networking.localization.Locale


object DevengNetworkingModule {
    var BASE_URL: String = ""

    val client = NetworkModule.httpClient

    var locale = Locale.TURKISH

    fun setBaseUrl(baseUrl: String) {
        BASE_URL = baseUrl
    }


    suspend inline fun <reified T, reified R> sendRequest(
        endpoint: String,
        requestBody: T? = null,
        requestMethod: HttpMethod
    ): Result<R> {
        return try {
            val response: HttpResponse = client.request("$BASE_URL$endpoint") {
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
                    val error = handleHttpException(locale, response.status)
                    throw DevengException(error)
                }
            }
        } catch (e: Exception) {
            print(e)
            val error = handleNetworkException(locale, e)
            throw DevengException(error)
        }
    }


    fun handleHttpException(locale: Locale, status: HttpStatusCode): DevengUiError {
        val errorKey = when (status) {
            HttpStatusCode.Unauthorized -> ErrorKey.UNAUTHORIZED
            HttpStatusCode.NotFound -> ErrorKey.NOT_FOUND
            HttpStatusCode.InternalServerError -> ErrorKey.SERVER_ERROR
            else -> ErrorKey.UNKNOWN_ERROR
        }
        return DevengUiError.createError(errorKey, locale, status.value.toString())
    }

    fun handleNetworkException(locale: Locale, cause: Throwable): DevengUiError {
        return DevengUiError.createError(
            ErrorKey.NETWORK_ERROR,
            locale,
            cause.message ?: "Unknown cause"
        )
    }

}

