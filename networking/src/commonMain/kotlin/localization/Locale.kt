package networking.localization;

enum class Locale {
    ENGLISH,
    TURKISH;

    companion object {
        fun fromCode(code: String): Locale {
            return when (code.lowercase()) {
                "en" -> ENGLISH
                "tr" -> TURKISH
                else -> ENGLISH // Default to English
            }
        }
    }
}