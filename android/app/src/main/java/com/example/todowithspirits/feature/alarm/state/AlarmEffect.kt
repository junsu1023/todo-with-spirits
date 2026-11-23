package com.example.todowithspirits.feature.alarm.state

sealed interface AlarmEffect {
    data object NavigateBack : AlarmEffect
    data object NavigateToSetting : AlarmEffect
}
