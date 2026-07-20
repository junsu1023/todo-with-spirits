package com.example.domain.repository

import com.example.domain.model.LoginSession

interface LoginRepository {
    suspend fun login(email: String, password: String): Result<LoginSession>

    suspend fun logout(): Result<Unit>
}