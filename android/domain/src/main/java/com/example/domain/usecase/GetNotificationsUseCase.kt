package com.example.domain.usecase

import com.example.domain.model.NotificationPage
import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(size: Int? = null, cursor: String? = null): Result<NotificationPage> =
        notificationRepository.getNotifications(size, cursor)
}
