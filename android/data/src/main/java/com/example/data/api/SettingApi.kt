package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.UpdateDisplaySettingRequest
import com.example.data.response.ApiResponse
import com.example.data.response.DisplaySettingResponse
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
}
