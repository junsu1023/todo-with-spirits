package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.response.ApiResponse
import com.example.data.response.DailyRecordResponse
import com.example.data.response.WeeklyRecordResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RecordApi {
    @GET(URLConstant.RECORD.RECORD_TODAY)
    suspend fun getTodayRecord(): Response<ApiResponse<DailyRecordResponse>>

    @GET(URLConstant.RECORD.RECORD_WEEKLY)
    suspend fun getWeeklyRecord(@Query("date") date: String): Response<ApiResponse<WeeklyRecordResponse>>
}
