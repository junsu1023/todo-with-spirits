package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.UpdateDisplaySettingRequest
import com.example.data.request.UpdateNotificationSettingRequest
import com.example.data.response.ApiResponse
import com.example.data.response.DisplaySettingResponse
import com.example.data.response.NotificationSettingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface SettingApi {
    @GET(URLConstant.SETTING.SETTING_DISPLAY)
    suspend fun getDisplaySetting(): Response<ApiResponse<DisplaySettingResponse>>

    @PATCH(URLConstant.SETTING.SETTING_DISPLAY)
    suspend fun updateDisplaySetting(
        @Body request: UpdateDisplaySettingRequest
    ): Response<ApiResponse<DisplaySettingResponse>>

    @GET(URLConstant.SETTING.SETTING_NOTIFICATION)
    suspend fun getNotificationSetting(): Response<ApiResponse<NotificationSettingResponse>>

    // 문서상 수정 응답 본문이 조회 응답과 형태가 달라(reminderTime 등) 본문은 쓰지 않는다.
    @PATCH(URLConstant.SETTING.SETTING_NOTIFICATION)
    suspend fun updateNotificationSetting(
        @Body request: UpdateNotificationSettingRequest
    ): Response<ApiResponse<Unit?>>
}
