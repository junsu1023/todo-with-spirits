package com.example.todowithspirits.feature.setting.alarm.state

import androidx.annotation.StringRes
import com.example.todowithspirits.R

// 화면에 표시되는 순서대로 둔다.
enum class AlarmSettingType(@StringRes val labelRes: Int) {
    ACTIVE_REMIND(R.string.inactivity_reminder_alarm),
    POSTPONE_PLAN(R.string.periodic_reminder_postponed_alarm),
    ROUTINE_GUIDE(R.string.routine_alarm),
    STREAK_SAVE(R.string.strick_save_alarm),
    NIGHT_PUSH(R.string.receive_night_time_push_alarm)
}
