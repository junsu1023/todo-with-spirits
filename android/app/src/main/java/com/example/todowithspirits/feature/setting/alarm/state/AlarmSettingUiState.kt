package com.example.todowithspirits.feature.setting.alarm.state

import androidx.compose.runtime.Immutable

@Immutable
data class AlarmSettingUiState(
    val isOnActiveRemind: Boolean = true,
    val isOnPostponePlan: Boolean = true,
    val isOnRoutineGuide: Boolean = true,
    val isOnStreakSave: Boolean = true,
    val isOnNightPush: Boolean = true
) {
    fun isOn(type: AlarmSettingType): Boolean = when (type) {
        AlarmSettingType.ACTIVE_REMIND -> isOnActiveRemind
        AlarmSettingType.POSTPONE_PLAN -> isOnPostponePlan
        AlarmSettingType.ROUTINE_GUIDE -> isOnRoutineGuide
        AlarmSettingType.STREAK_SAVE -> isOnStreakSave
        AlarmSettingType.NIGHT_PUSH -> isOnNightPush
    }
}