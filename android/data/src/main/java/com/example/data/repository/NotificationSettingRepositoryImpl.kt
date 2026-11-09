package com.example.data.repository

import com.example.data.datasource.SettingRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.data.request.UpdateNotificationSettingRequest
import com.example.domain.model.NotificationSetting
import com.example.domain.repository.NotificationSettingRepository
import javax.inject.Inject

class NotificationSettingRepositoryImpl @Inject constructor(
    private val settingRemoteDataSource: SettingRemoteDataSource
) : NotificationSettingRepository {
    override suspend fun getNotificationSetting(): Result<NotificationSetting> =
        settingRemoteDataSource.getNotificationSetting().mapCatching { it.toDomain() }

    override suspend fun updateNotificationSetting(
        reminderEnabled: Boolean?,
        postponedTaskEnabled: Boolean?,
        routineReminderEnabled: Boolean?,
        streakSaveEnabled: Boolean?,
        nightPushEnabled: Boolean?
    ): Result<Unit> =
        settingRemoteDataSource.updateNotificationSetting(
            UpdateNotificationSettingRequest(
                reminderEnabled = reminderEnabled,
                postponedTaskEnabled = postponedTaskEnabled,
                routineReminderEnabled = routineReminderEnabled,
                streakSaveEnabled = streakSaveEnabled,
                nightPushEnabled = nightPushEnabled
            )
        )
}
