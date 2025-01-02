package networking.exception_handling

import error_handling.DevengUiError
import error_handling.ErrorKey
import io.ktor.http.HttpStatusCode
import networking.localization.Locale

internal object ExceptionHandlerImpl : ExceptionHandler {
    override var locale: Locale = Locale.TR

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
            locale = locale,
            apiErrorMessage = errorMessage
        )
    }

    override fun handleNetworkException(cause: Throwable): DevengUiError {
        if (locale == Locale.TR) {
            return DevengUiError.NetworkError("Bir hata oluştu.")
        }
        return DevengUiError.NetworkError("An error occurred.")
    }
}