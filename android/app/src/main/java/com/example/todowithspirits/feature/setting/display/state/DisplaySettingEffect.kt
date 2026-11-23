package com.example.todowithspirits.feature.setting.display.state

sealed interface DisplaySettingEffect {
    data object NavigateBack : DisplaySettingEffect
}
