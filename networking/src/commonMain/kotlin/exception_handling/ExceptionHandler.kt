package networking.exception_handling

import error_handling.DevengUiError
import io.ktor.http.HttpStatusCode
import networking.localization.Locale

interface ExceptionHandler {
    var locale: Locale

    fun handleHttpException(
        status: HttpStatusCode
    ): DevengUiError

    fun handleNetworkException(
        cause: Throwable
    ): DevengUiError

}