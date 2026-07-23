package com.example.todowithspirits.feature.setting.state

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
