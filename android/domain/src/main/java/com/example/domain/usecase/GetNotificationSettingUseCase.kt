package com.example.domain.usecase

import com.example.domain.model.NotificationSetting
import com.example.domain.repository.NotificationSettingRepository
import javax.inject.Inject

class GetNotificationSettingUseCase @Inject constructor(
    private val notificationSettingRepository: NotificationSettingRepository
) {
    suspend operator fun invoke(): Result<NotificationSetting> =
        notificationSettingRepository.getNotificationSetting()
}
