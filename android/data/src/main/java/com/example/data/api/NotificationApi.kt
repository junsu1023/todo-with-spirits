package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.response.ApiResponse
import com.example.data.response.NotificationPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET(URLConstant.NOTIFICATION.NOTIFICATION)
    suspend fun getNotifications(
        @Query("size") size: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<ApiResponse<NotificationPageResponse>>

    @PATCH(URLConstant.NOTIFICATION.NOTIFICATION_READ)
    suspend fun markNotificationRead(@Path("alarmId") alarmId: Long): Response<ApiResponse<Unit?>>

    @PATCH(URLConstant.NOTIFICATION.NOTIFICATION_READ_ALL)
    suspend fun markAllNotificationsRead(): Response<ApiResponse<Unit?>>
}
