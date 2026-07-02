package com.example.data.datasource

import com.example.data.api.HealthApi
import com.example.data.response.HealthResponse
import retrofit2.Response
import javax.inject.Inject

class CheckSystemHealthRemoteDataSource @Inject constructor(
    private val healthApi: HealthApi
) {
    suspend fun getHealth(): Response<HealthResponse> = healthApi.getHealth()
}