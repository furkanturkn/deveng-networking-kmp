package networking.localization

import error_handling.ErrorKey

internal object LocalizationManager {
    private var customTranslations: Map<Locale, Map<ErrorKey, String>>? = null

    private val defaultTranslations: Map<Locale, Map<ErrorKey, String>> = mapOf(
        Locale.EN to mapOf(
            ErrorKey.UNAUTHORIZED to "Unauthorized access",
            ErrorKey.NOT_FOUND to "Resource not found",
            ErrorKey.SERVER_ERROR to "Server error occurred",
            ErrorKey.UNKNOWN_ERROR to "Unknown error: %s",
            ErrorKey.NETWORK_ERROR to "Network error: %s",
            ErrorKey.CONNECTION_ERROR to "Please check your internet connection. Unable to reach the server."
        ),
        Locale.TR to mapOf(
            ErrorKey.UNAUTHORIZED to "Yetkisiz erişim",
            ErrorKey.NOT_FOUND to "Kaynak bulunamadı",
            ErrorKey.SERVER_ERROR to "Sunucu hatası meydana geldi",
            ErrorKey.UNKNOWN_ERROR to "Bilinmeyen hata: %s",
            ErrorKey.NETWORK_ERROR to "Ağ hatası: %s",
            ErrorKey.CONNECTION_ERROR to "İnternet bağlantınızı kontrol edin. Sunucuya ulaşılamıyor."
        )
    )

    internal fun setCustomTranslations(translations: Map<Locale, Map<ErrorKey, String>>) {
        customTranslations = translations
    }

    internal fun getLocalizedError(locale: Locale, errorKey: ErrorKey, vararg formatArgs: Any): String {
        // First check custom translations, then fall back to default
        val translationMap = customTranslations?.get(locale) 
            ?: defaultTranslations[locale] 
            ?: defaultTranslations[Locale.EN]!!
            
        val template = translationMap[errorKey] 
            ?: defaultTranslations[locale]?.get(errorKey)
            ?: defaultTranslations[Locale.EN]?.get(errorKey)
            ?: "Unknown error"

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
        if (formatArgs.isEmpty()) {
            result = result.replace("%s", "")
        }
        return result
    }
}