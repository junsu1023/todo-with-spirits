package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.LoginRequest
import com.example.data.request.SendEmailVerificationRequest
import com.example.data.request.SignUpRequest
import com.example.data.request.SocialLoginRequest
import com.example.data.request.UpdateUserProfileRequest
import com.example.data.request.VerifyEmailCodeRequest
import com.example.data.response.ApiResponse
import com.example.data.response.EmailAvailabilityResponse
import com.example.data.response.LoginResponse
import com.example.data.response.SignUpResponse
import com.example.data.response.SocialLoginResponse
import com.example.data.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @GET(URLConstant.LOGIN.CHECK_EMAIL)
    suspend fun checkEmail(@Query("email") email: String): Response<ApiResponse<EmailAvailabilityResponse>>

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

    @DELETE(URLConstant.USER.USER_ME)
    suspend fun withdraw(): Response<ApiResponse<Unit?>>

    @PATCH(URLConstant.USER.USER_ME)
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): Response<ApiResponse<UserProfileResponse>>

    @POST(URLConstant.USER.EMAIL_VERIFY_RESEND)
    suspend fun resendEmailVerification(): Response<ApiResponse<Unit?>>

    @POST(URLConstant.USER.EMAIL_VERIFY_SEND)
    suspend fun sendEmailVerification(@Body request: SendEmailVerificationRequest): Response<ApiResponse<Unit?>>

    @POST(URLConstant.USER.EMAIL_VERIFY)
    suspend fun verifyEmailCode(@Body request: VerifyEmailCodeRequest): Response<ApiResponse<Unit?>>
}