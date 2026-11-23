package com.example.todowithspirits.feature.setting.main.state

sealed interface MainSettingEffect {
    data object NavigateBack : MainSettingEffect
    data class NavigateToMenu(val menu: SettingMenu) : MainSettingEffect
}
