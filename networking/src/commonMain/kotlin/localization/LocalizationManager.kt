package networking.localization

import error_handling.ErrorKey

internal object LocalizationManager {

    private val translations: Map<Locale, Map<ErrorKey, String>> = mapOf(
        Locale.EN to mapOf(
            ErrorKey.UNAUTHORIZED to "Unauthorized access",
            ErrorKey.NOT_FOUND to "Resource not found",
            ErrorKey.SERVER_ERROR to "Server error occurred",
            ErrorKey.UNKNOWN_ERROR to "Unknown error: %s",
            ErrorKey.NETWORK_ERROR to "Network error: %s"
        ),
        Locale.TR to mapOf(
            ErrorKey.UNAUTHORIZED to "Yetkisiz erişim",
            ErrorKey.NOT_FOUND to "Kaynak bulunamadı",
            ErrorKey.SERVER_ERROR to "Sunucu hatası meydana geldi",
            ErrorKey.UNKNOWN_ERROR to "Bilinmeyen hata: %s",
            ErrorKey.NETWORK_ERROR to "Ağ hatası: %s"
        )
    )

    fun getLocalizedError(locale: Locale, errorKey: ErrorKey, vararg formatArgs: Any): String {
        val translationMap = translations[locale] ?: translations[Locale.EN]!!
        val template = translationMap[errorKey] ?: "Unknown error"

        return if (formatArgs.isNotEmpty()) {
            formatTemplate(template, formatArgs)
        } else {
            template
        }
    }

    private fun formatTemplate(template: String, formatArgs: Array<out Any>): String {
        var result = template
        formatArgs.forEach { arg ->
            result = result.replaceFirst("%s", arg.toString())
        }
        return result
    }
}