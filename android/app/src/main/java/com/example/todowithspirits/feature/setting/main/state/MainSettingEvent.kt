package com.example.todowithspirits.feature.setting.main.state

sealed interface MainSettingEvent {
    data object BackClicked : MainSettingEvent
    data class MenuClicked(val menu: SettingMenu) : MainSettingEvent
}
