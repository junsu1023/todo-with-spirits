package com.example.todowithspirits.feature.setting.main.state

sealed interface MainSettingEvent {
    // 화면이 다시 보일 때(다른 설정 화면에서 돌아왔을 때 포함)
    data object ScreenResumed : MainSettingEvent
    data object BackClicked : MainSettingEvent
    data class MenuClicked(val menu: SettingMenu) : MainSettingEvent
}
