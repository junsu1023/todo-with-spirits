package com.example.data.datasource

import com.example.data.api.AuthApi
import com.example.data.network.apiCall
import com.example.data.network.apiCallUnit
import com.example.data.request.LoginRequest
import com.example.data.request.SendEmailVerificationRequest
import com.example.data.request.SignUpRequest
import com.example.data.request.SocialLoginRequest
import com.example.data.request.UpdateUserProfileRequest
import com.example.data.request.VerifyEmailCodeRequest
import com.example.data.response.EmailAvailabilityResponse
import com.example.data.response.LoginResponse
import com.example.data.response.SignUpResponse
import com.example.data.response.SocialLoginResponse
import com.example.data.response.UserProfileResponse
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi
) {
    suspend fun checkEmail(email: String): Result<EmailAvailabilityResponse> =
        apiCall { authApi.checkEmail(email) }

    suspend fun login(email: String, password: String): Result<LoginResponse> =
        apiCall { authApi.login(LoginRequest(email, password)) }

    suspend fun socialLogin(
        provider: String,
        providerUserId: String,
        providerAccessToken: String,
        email: String?
    ): Result<SocialLoginResponse> =
        apiCall {
            authApi.socialLogin(
                providerAuthorization = "Bearer $providerAccessToken",
                request = SocialLoginRequest(provider, providerUserId, email)
            )
        }

    suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResponse> =
        apiCall { authApi.signUp(SignUpRequest(email, password, nickname)) }

    suspend fun logout(): Result<Unit> = apiCallUnit { authApi.logout() }

    suspend fun withdraw(): Result<Unit> = apiCallUnit { authApi.withdraw() }

    suspend fun updateUserProfile(nickname: String?, representativeSpiritId: Long?): Result<UserProfileResponse> =
        apiCall { authApi.updateUserProfile(UpdateUserProfileRequest(nickname, representativeSpiritId)) }

    suspend fun resendEmailVerification(): Result<Unit> = apiCallUnit { authApi.resendEmailVerification() }

    suspend fun sendEmailVerification(email: String): Result<Unit> =
        apiCallUnit { authApi.sendEmailVerification(SendEmailVerificationRequest(email)) }

    suspend fun verifyEmailCode(email: String, code: Int): Result<Unit> =
        apiCallUnit { authApi.verifyEmailCode(VerifyEmailCodeRequest(email, code)) }
}
