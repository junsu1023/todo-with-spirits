package com.example.todowithspirits.feature.setting.state

import com.example.domain.model.AppLanguage

data class DisplaySettingUiState(
    val isDarkMode: Boolean = false,
    val isShowPlanDday: Boolean = true,
    val planSortOption: String = PlanSortOptions.first(),
    val themeOption: String = ThemeOptions.first(),
    val languageOption: String = LanguageOptions.first()
)

val PlanSortOptions = listOf("시간 순", "완료 순")
val ThemeOptions = listOf("시스템", "다크모드", "라이트모드")

val LanguageOptions = listOf("시스템", "한국어", "English", "일본어", "중국어")

private val languageOptionToAppLanguage = mapOf(
    "시스템" to AppLanguage.SYSTEM,
    "한국어" to AppLanguage.KOREAN,
    "English" to AppLanguage.ENGLISH,
    "일본어" to AppLanguage.JAPANESE,
    "중국어" to AppLanguage.CHINESE
)

fun String.toAppLanguage(): AppLanguage = languageOptionToAppLanguage[this] ?: AppLanguage.SYSTEM

fun AppLanguage.toLanguageOption(): String =
    languageOptionToAppLanguage.entries.find { it.value == this }?.key ?: LanguageOptions.first()
