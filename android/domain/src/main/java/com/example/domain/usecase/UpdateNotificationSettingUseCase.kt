package com.example.domain.usecase

import com.example.domain.repository.NotificationSettingRepository
import javax.inject.Inject

class UpdateNotificationSettingUseCase @Inject constructor(
    private val notificationSettingRepository: NotificationSettingRepository
) {
    suspend operator fun invoke(
        reminderEnabled: Boolean? = null,
        postponedTaskEnabled: Boolean? = null,
        routineReminderEnabled: Boolean? = null,
        streakSaveEnabled: Boolean? = null,
        nightPushEnabled: Boolean? = null
    ): Result<Unit> = notificationSettingRepository.updateNotificationSetting(
        reminderEnabled,
        postponedTaskEnabled,
        routineReminderEnabled,
        streakSaveEnabled,
        nightPushEnabled
    )
}
