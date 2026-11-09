package com.example.domain.model

data class NotificationSetting(
    val reminderEnabled: Boolean, // 리마인드 알림
    val postponedTaskEnabled: Boolean, // 미룬 플랜 정기 알림
    val routineReminderEnabled: Boolean, // 루틴 안내 알림
    val streakSaveEnabled: Boolean, // 스트릭(연속 달성) 세이브 알림
    val nightPushEnabled: Boolean // 야간 푸시 알림 수신
)
