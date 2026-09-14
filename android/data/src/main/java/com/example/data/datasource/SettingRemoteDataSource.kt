package com.example.data.datasource

import com.example.data.api.SettingApi
import com.example.data.network.apiCall
import com.example.data.request.UpdateDisplaySettingRequest
import com.example.data.response.DisplaySettingResponse
import javax.inject.Inject

class SettingRemoteDataSource @Inject constructor(
    private val settingApi: SettingApi
) {
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
}
