package com.example.domain.repository

import com.example.domain.model.NotificationSetting

interface NotificationSettingRepository {
    suspend fun getNotificationSetting(): Result<NotificationSetting>

    // Optional 파라미터: 최소 1개 이상 전달, 나머지는 null(변경 없음)로 서버에 그대로 전달된다.
    suspend fun updateNotificationSetting(
        reminderEnabled: Boolean? = null,
        postponedTaskEnabled: Boolean? = null,
        routineReminderEnabled: Boolean? = null,
        streakSaveEnabled: Boolean? = null,
        nightPushEnabled: Boolean? = null
    ): Result<Unit>
}
