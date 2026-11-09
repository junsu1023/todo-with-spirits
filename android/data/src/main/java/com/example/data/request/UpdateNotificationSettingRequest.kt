package com.example.data.request

data class UpdateNotificationSettingRequest(
    val reminderEnabled: Boolean? = null,
    val postponedTaskEnabled: Boolean? = null,
    val routineReminderEnabled: Boolean? = null,
    val streakSaveEnabled: Boolean? = null,
    val nightPushEnabled: Boolean? = null
)
