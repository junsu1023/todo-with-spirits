package com.example.todowithspirits.feature.setting.alarm.state

sealed interface AlarmSettingEvent {
    data object BackClicked : AlarmSettingEvent
    data object ConfirmClicked : AlarmSettingEvent
    data class AlarmToggled(val type: AlarmSettingType, val isOn: Boolean) : AlarmSettingEvent
}
