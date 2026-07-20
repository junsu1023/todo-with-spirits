package com.example.data.repository

import com.example.core.auth.TokenHolder
import com.example.data.datasource.AuthRemoteDataSource
import com.example.data.datasource.LoginRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.LoginSession
import com.example.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val loginRemoteDataSource: LoginRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource
) : LoginRepository {
    override suspend fun login(email: String, password: String): Result<LoginSession> {
        return loginRemoteDataSource.login(email, password)
            .map { it.toDomain() }
            .onSuccess { session -> TokenHolder.accessToken = session.accessToken }
    }

    override suspend fun logout(): Result<Unit> {
        return authRemoteDataSource.logout()
            .onSuccess { TokenHolder.accessToken = null }
    }
}