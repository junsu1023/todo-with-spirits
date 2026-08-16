package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.LoginRequest
import com.example.data.request.SignUpRequest
import com.example.data.request.SocialLoginRequest
import com.example.data.response.ApiResponse
import com.example.data.response.LoginResponse
import com.example.data.response.SignUpResponse
import com.example.data.response.SocialLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST(URLConstant.LOGIN.LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST(URLConstant.LOGIN.SOCIAL_LOGIN)
    suspend fun socialLogin(
        @Header("Authorization") providerAuthorization: String,
        @Body request: SocialLoginRequest
    ): Response<ApiResponse<SocialLoginResponse>>

    @POST(URLConstant.LOGIN.SIGNUP)
    suspend fun signUp(@Body request: SignUpRequest): Response<ApiResponse<SignUpResponse>>

    @POST(URLConstant.LOGIN.LOGOUT)
    suspend fun logout(): Response<ApiResponse<Unit?>>
}