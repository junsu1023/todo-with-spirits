package com.example.domain.repository

import com.example.domain.model.NotificationPage

interface NotificationRepository {
    suspend fun getNotifications(size: Int? = null, cursor: String? = null): Result<NotificationPage>
}
