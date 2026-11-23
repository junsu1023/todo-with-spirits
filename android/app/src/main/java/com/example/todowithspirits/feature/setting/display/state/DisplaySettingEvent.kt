package com.example.todowithspirits.feature.setting.display.state

sealed interface DisplaySettingEvent {
    data object BackClicked : DisplaySettingEvent
    data object ConfirmClicked : DisplaySettingEvent
    data class DarkModeToggled(val isOn: Boolean) : DisplaySettingEvent
    data class PlanDdayToggled(val isOn: Boolean) : DisplaySettingEvent
    data class LanguageSelected(val option: String) : DisplaySettingEvent
}
