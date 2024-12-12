package error_handling

import networking.localization.Locale
import networking.localization.LocalizationManager

sealed class DevengUiError(val errorMessage: String) : Throwable(errorMessage) {
    class HttpError(message: String) : DevengUiError(message)
    class NetworkError(message: String) : DevengUiError(message)
    class UnauthorizedError(message: String) : DevengUiError(message)
    class NotFoundError(message: String) : DevengUiError(message)
    class ServerError(message: String) : DevengUiError(message)
    class UnknownError(message: String) : DevengUiError(message)

    companion object {
        fun createError(
            key: ErrorKey,
            locale: Locale,
            vararg args: Any
        ): DevengUiError {
            val localizedMessage = LocalizationManager.getLocalizedError(locale, key, *args)
            return when (key) {
                ErrorKey.UNAUTHORIZED -> UnauthorizedError(localizedMessage)
                ErrorKey.NOT_FOUND -> NotFoundError(localizedMessage)
                ErrorKey.SERVER_ERROR -> ServerError(localizedMessage)
                ErrorKey.UNKNOWN_ERROR -> UnknownError(localizedMessage)
                ErrorKey.NETWORK_ERROR -> NetworkError(localizedMessage)
            }
        }
    }
}

class DevengException(val error: DevengUiError) : Exception(error.message)