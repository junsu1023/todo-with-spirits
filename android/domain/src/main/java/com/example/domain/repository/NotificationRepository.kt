package com.example.domain.repository

import com.example.domain.model.NotificationPage

interface NotificationRepository {
    suspend fun getNotifications(size: Int? = null, cursor: String? = null): Result<NotificationPage>

    suspend fun markNotificationRead(alarmId: Long): Result<Unit>

    suspend fun markAllNotificationsRead(): Result<Unit>
}
