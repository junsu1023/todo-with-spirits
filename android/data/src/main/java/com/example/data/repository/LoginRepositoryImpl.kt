package com.example.data.repository

import com.example.core.auth.TokenHolder
import com.example.data.datasource.LoginRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.LoginSession
import com.example.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val loginRemoteDataSource: LoginRemoteDataSource
) : LoginRepository {
    override suspend fun login(email: String, password: String): Result<LoginSession> {
        return loginRemoteDataSource.login(email, password)
            .map { it.toDomain() }
            .onSuccess { session -> TokenHolder.accessToken = session.accessToken }
    }
}