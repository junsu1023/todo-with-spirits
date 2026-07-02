package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.response.HealthResponse
import retrofit2.Response
import retrofit2.http.GET

interface HealthApi {
    @GET(URLConstant.HEALTH.HEALTH)
    suspend fun getHealth(): Response<HealthResponse>
}