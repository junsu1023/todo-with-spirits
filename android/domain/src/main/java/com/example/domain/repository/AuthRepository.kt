package com.example.domain.repository

import com.example.domain.model.LoginSession
import com.example.domain.model.SignUpResult
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginSession>

    suspend fun socialLogin(
        provider: SocialProvider,
        providerUserId: String,
        providerAccessToken: String,
        email: String?
    ): Result<SocialLoginSession>

    suspend fun logout(): Result<Unit>

    suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResult>

    suspend fun restoreSession(): Boolean
}
