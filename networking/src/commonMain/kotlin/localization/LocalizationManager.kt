package networking.localization

import error_handling.ErrorKey

internal object LocalizationManager {
    private var customTranslations: Map<Locale, Map<ErrorKey, String>>? = null

    private val defaultTranslations: Map<Locale, Map<ErrorKey, String>> = mapOf(
        Locale.EN to mapOf(
            ErrorKey.UNAUTHORIZED to "Unauthorized access",
            ErrorKey.NOT_FOUND to "Resource not found",
            ErrorKey.SERVER_ERROR to "Server error occurred",
            ErrorKey.UNKNOWN_ERROR to "Unknown error",
            ErrorKey.NETWORK_ERROR to "Network error",
            ErrorKey.CONNECTION_ERROR to "Please check your internet connection. Unable to reach the server."
        ),
        Locale.TR to mapOf(
            ErrorKey.UNAUTHORIZED to "Yetkisiz erişim",
            ErrorKey.NOT_FOUND to "Kaynak bulunamadı",
            ErrorKey.SERVER_ERROR to "Sunucu hatası meydana geldi",
            ErrorKey.UNKNOWN_ERROR to "Bilinmeyen hata",
            ErrorKey.NETWORK_ERROR to "Ağ hatası",
            ErrorKey.CONNECTION_ERROR to "İnternet bağlantınızı kontrol edin. Sunucuya ulaşılamıyor."
        )
    )

    internal fun setCustomTranslations(translations: Map<Locale, Map<ErrorKey, String>>) {
        customTranslations = translations
    }

    internal fun getLocalizedError(locale: Locale, errorKey: ErrorKey): String {
        // First check custom translations, then fall back to default
        val translationMap = customTranslations?.get(locale) 
            ?: defaultTranslations[locale] 
            ?: defaultTranslations[Locale.EN]!!
            
        return translationMap[errorKey] 
            ?: defaultTranslations[locale]?.get(errorKey)
            ?: defaultTranslations[Locale.EN]?.get(errorKey)
            ?: "Unknown error"
    }

}