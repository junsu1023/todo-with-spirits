package com.example.todowithspirits.feature.setting.alarm.state

sealed interface AlarmSettingEffect {
    data object NavigateBack : AlarmSettingEffect
}
