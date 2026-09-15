package com.example.domain.repository

import com.example.domain.model.EmailAvailability
import com.example.domain.model.LoginMethod
import com.example.domain.model.LoginSession
import com.example.domain.model.SignUpResult
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider
import com.example.domain.model.UserProfile

interface AuthRepository {
    suspend fun checkEmail(email: String): Result<EmailAvailability>

    suspend fun login(email: String, password: String): Result<LoginSession>

    suspend fun socialLogin(
        provider: SocialProvider,
        providerUserId: String,
        providerAccessToken: String,
        email: String?
    ): Result<SocialLoginSession>

    suspend fun logout(): Result<Unit>

    suspend fun withdraw(): Result<Unit>

    suspend fun getUserProfile(): Result<UserProfile>

    suspend fun updateUserProfile(nickname: String? = null, representativeSpiritId: Long? = null): Result<UserProfile>

    suspend fun resendEmailVerification(): Result<Unit>

    suspend fun sendEmailVerification(email: String): Result<Unit>

    suspend fun verifyEmailCode(email: String, code: Int): Result<Unit>

    suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResult>

    suspend fun restoreSession(): Boolean

    fun getLoginMethod(): LoginMethod?
}
