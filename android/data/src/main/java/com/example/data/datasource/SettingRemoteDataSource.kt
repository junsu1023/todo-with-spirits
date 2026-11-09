package com.example.data.datasource

import com.example.data.api.SettingApi
import com.example.data.network.apiCall
import com.example.data.network.apiCallUnit
import com.example.data.request.UpdateDisplaySettingRequest
import com.example.data.request.UpdateNotificationSettingRequest
import com.example.data.response.DisplaySettingResponse
import com.example.data.response.NotificationSettingResponse
import javax.inject.Inject

class SettingRemoteDataSource @Inject constructor(
    private val settingApi: SettingApi
) {
    suspend fun getDisplaySetting(): Result<DisplaySettingResponse> =
        apiCall { settingApi.getDisplaySetting() }

    suspend fun updateDisplaySetting(
        darkMode: Boolean?,
        ddayDisplayEnabled: Boolean?,
        language: String?
    ): Result<DisplaySettingResponse> =
        apiCall {
            settingApi.updateDisplaySetting(
                UpdateDisplaySettingRequest(darkMode, ddayDisplayEnabled, language)
            )
        }

    suspend fun getNotificationSetting(): Result<NotificationSettingResponse> =
        apiCall { settingApi.getNotificationSetting() }

    suspend fun updateNotificationSetting(request: UpdateNotificationSettingRequest): Result<Unit> =
        apiCallUnit { settingApi.updateNotificationSetting(request) }
}
