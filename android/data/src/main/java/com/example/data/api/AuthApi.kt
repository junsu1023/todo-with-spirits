package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.response.ApiResponse
import retrofit2.Response
import retrofit2.http.POST

interface AuthApi {
    @POST(URLConstant.LOGIN.LOGOUT)
    suspend fun logout(): Response<ApiResponse<Unit?>>
}
