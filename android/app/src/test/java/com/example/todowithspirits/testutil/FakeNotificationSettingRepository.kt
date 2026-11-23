package com.example.todowithspirits.testutil

import com.example.domain.model.NotificationSetting
import com.example.domain.repository.NotificationSettingRepository

// 알림 설정 테스트용 NotificationSettingRepository 더블.
class FakeNotificationSettingRepository(
    private val setting: NotificationSetting,
    private val updateResult: Result<Unit> = Result.success(Unit)
) : NotificationSettingRepository {
    data class UpdateRequest(
        val reminderEnabled: Boolean?,
        val postponedTaskEnabled: Boolean?,
        val routineReminderEnabled: Boolean?,
        val streakSaveEnabled: Boolean?,
        val nightPushEnabled: Boolean?
    )

    val updateRequests = mutableListOf<UpdateRequest>()

    override suspend fun getNotificationSetting(): Result<NotificationSetting> = Result.success(setting)

    override suspend fun updateNotificationSetting(
        reminderEnabled: Boolean?,
        postponedTaskEnabled: Boolean?,
        routineReminderEnabled: Boolean?,
        streakSaveEnabled: Boolean?,
        nightPushEnabled: Boolean?
    ): Result<Unit> {
        updateRequests += UpdateRequest(
            reminderEnabled, postponedTaskEnabled, routineReminderEnabled, streakSaveEnabled, nightPushEnabled
        )
        return updateResult
    }
}
