package networking.exception_handling

import error_handling.DevengUiError
import error_handling.ErrorKey
import io.ktor.http.HttpStatusCode
import networking.localization.Locale

internal object ExceptionHandlerImpl : ExceptionHandler {
    override var locale: Locale? = null

    override fun handleHttpException(
        errorMessage: String?,
        status: HttpStatusCode
    ): DevengUiError {
        val errorKey = when (status) {
            HttpStatusCode.Unauthorized -> ErrorKey.UNAUTHORIZED
            HttpStatusCode.NotFound -> ErrorKey.NOT_FOUND
            HttpStatusCode.InternalServerError -> ErrorKey.SERVER_ERROR
            else -> ErrorKey.UNKNOWN_ERROR
        }
        return DevengUiError.createError(
            key = errorKey,
            locale = locale ?: Locale.TR,
            apiErrorMessage = errorMessage
        )
    }

    override fun handleNetworkException(cause: Throwable): DevengUiError {
        val isUnknownHostException = cause::class.simpleName == "UnknownHostException" || 
                                   cause.cause?.let { it::class.simpleName == "UnknownHostException" } == true ||
                                   cause.message?.contains("Unable to resolve host") == true ||
                                   cause.message?.contains("No address associated with hostname") == true
        
        return if (isUnknownHostException) {
            DevengUiError.createError(
                key = ErrorKey.CONNECTION_ERROR,
                locale = locale ?: Locale.EN,
                apiErrorMessage = null
            )
        } else {
            DevengUiError.createError(
                key = ErrorKey.NETWORK_ERROR,
                locale = locale ?: Locale.EN,
                apiErrorMessage = null
            )
        }
    }
}