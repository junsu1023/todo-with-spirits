package com.example.domain.model

data class DisplaySetting(
    val darkMode: Boolean,
    val ddayDisplayEnabled: Boolean,
    val language: AppLanguage
)

enum class AppLanguage(val apiValue: String) {
    SYSTEM("system"),
    KOREAN("ko"),
    ENGLISH("en"),
    JAPANESE("ja"),
    CHINESE("zh");

    companion object {
        fun fromApiValue(value: String): AppLanguage =
            entries.find { it.apiValue == value } ?: SYSTEM
    }
}
