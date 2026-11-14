package com.example.data.response

data class NotificationSettingResponse(
    val reminderEnabled: Boolean,
    val postponedTaskEnabled: Boolean,
    val routineReminderEnabled: Boolean,
    val streakSaveEnabled: Boolean,
    val nightPushEnabled: Boolean
)
