package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.LoginRequest
import com.example.data.response.ApiResponse
import com.example.data.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST(URLConstant.LOGIN.LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
}