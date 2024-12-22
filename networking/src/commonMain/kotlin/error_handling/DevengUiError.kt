package error_handling

import networking.localization.Locale
import networking.localization.LocalizationManager

public sealed class DevengUiError(errorMessage: String) : Throwable(errorMessage) {
    internal class HttpError(message: String) : DevengUiError(message)
    internal class NetworkError(message: String) : DevengUiError(message)
    public class UnauthorizedError(message: String) : DevengUiError(message)
    internal class NotFoundError(message: String) : DevengUiError(message)
    internal class ServerError(message: String) : DevengUiError(message)
    public class UnknownError(message: String) : DevengUiError(message)

    internal companion object {
        internal fun createError(
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

public class DevengException(error: DevengUiError) : Exception(error.message, error)
