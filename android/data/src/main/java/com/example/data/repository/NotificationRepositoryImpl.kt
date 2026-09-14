package com.example.data.repository

import com.example.data.datasource.NotificationRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.NotificationPage
import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationRemoteDataSource: NotificationRemoteDataSource
) : NotificationRepository {
    override suspend fun getNotifications(size: Int?, cursor: String?): Result<NotificationPage> =
        notificationRemoteDataSource.getNotifications(size, cursor).mapCatching { it.toDomain() }

    override suspend fun markNotificationRead(alarmId: Long): Result<Unit> =
        notificationRemoteDataSource.markNotificationRead(alarmId)

    override suspend fun markAllNotificationsRead(): Result<Unit> =
        notificationRemoteDataSource.markAllNotificationsRead()
}
