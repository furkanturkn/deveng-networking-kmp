package networking.exception_handling

import error_handling.DevengUiError
import io.ktor.http.HttpStatusCode
import networking.localization.Locale

public interface ExceptionHandler {
    public var locale: Locale

    public fun handleHttpException(
        errorMessage: String?,
        status: HttpStatusCode
    ): DevengUiError

    public fun handleNetworkException(
        cause: Throwable
    ): DevengUiError

}