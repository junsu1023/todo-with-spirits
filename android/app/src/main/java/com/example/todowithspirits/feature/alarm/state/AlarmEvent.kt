package com.example.todowithspirits.feature.alarm.state

sealed interface AlarmEvent {
    // 상단 뒤로가기와 시스템 뒤로가기
    data object BackClicked : AlarmEvent
    data object SettingClicked : AlarmEvent
}
