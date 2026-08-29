package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.response.ApiResponse
import com.example.data.response.DailyRecordResponse
import retrofit2.Response
import retrofit2.http.GET

interface RecordApi {
    @GET(URLConstant.RECORD.RECORD_TODAY)
    suspend fun getTodayRecord(): Response<ApiResponse<DailyRecordResponse>>
}
