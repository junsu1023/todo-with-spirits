package com.example.domain.usecase

import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(alarmId: Long): Result<Unit> = notificationRepository.markNotificationRead(alarmId)
}
