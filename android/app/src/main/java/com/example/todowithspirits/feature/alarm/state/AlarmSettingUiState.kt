package com.example.todowithspirits.feature.alarm.state

data class AlarmSettingUiState(
    val isOnActiveRemind: Boolean = false,
    val isOnPostponePlan: Boolean = false,
    val isOnRoutineGuide: Boolean = false,
    val isOnStreakSave: Boolean = false,
    val isOnNightPush: Boolean = false,
    val isOnPromotionConsent: Boolean = false
)
