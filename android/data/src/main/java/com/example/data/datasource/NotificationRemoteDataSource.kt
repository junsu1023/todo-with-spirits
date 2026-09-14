package com.example.data.datasource

import com.example.data.api.NotificationApi
import com.example.data.network.apiCall
import com.example.data.network.apiCallUnit
import com.example.data.response.NotificationPageResponse
import javax.inject.Inject

class NotificationRemoteDataSource @Inject constructor(
    private val notificationApi: NotificationApi
) {
    suspend fun getNotifications(size: Int?, cursor: String?): Result<NotificationPageResponse> =
        apiCall { notificationApi.getNotifications(size, cursor) }

    suspend fun markNotificationRead(alarmId: Long): Result<Unit> =
        apiCallUnit { notificationApi.markNotificationRead(alarmId) }

    suspend fun markAllNotificationsRead(): Result<Unit> =
        apiCallUnit { notificationApi.markAllNotificationsRead() }
}
