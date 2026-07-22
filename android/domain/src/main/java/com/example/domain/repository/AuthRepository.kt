package com.example.domain.repository

import com.example.domain.model.LoginSession
import com.example.domain.model.SignUpResult

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginSession>

    suspend fun logout(): Result<Unit>

    suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResult>
}
