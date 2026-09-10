package com.example.data.repository

import com.example.core.auth.TokenHolder
import com.example.core.auth.TokenStorage
import com.example.data.datasource.AuthRemoteDataSource
import com.example.data.error.ApiException
import com.example.data.mapper.toDomain
import com.example.domain.exception.FieldValidationException
import com.example.domain.model.LoginMethod
import com.example.domain.model.LoginSession
import com.example.domain.model.SignUpResult
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginSession> {
        return authRemoteDataSource.login(email, password)
            .map { it.toDomain() }
            .onSuccess { session ->
                persistTokens(session.accessToken, session.refreshToken)
                tokenStorage.saveLoginMethod(LoginMethod.EMAIL.name)
            }
    }

    override suspend fun socialLogin(
        provider: SocialProvider,
        providerUserId: String,
        providerAccessToken: String,
        email: String?
    ): Result<SocialLoginSession> {
        return authRemoteDataSource.socialLogin(provider.name, providerUserId, providerAccessToken, email)
            .mapCatching { it.toDomain() }
            .onSuccess { session ->
                persistTokens(session.accessToken, session.refreshToken)
                tokenStorage.saveLoginMethod(provider.toLoginMethod().name)
            }
            .recoverFieldValidationErrors()
    }

    override suspend fun logout(): Result<Unit> {
        return authRemoteDataSource.logout()
            .onSuccess { clearSession() }
    }

    override suspend fun withdraw(): Result<Unit> {
        return authRemoteDataSource.withdraw()
            .onSuccess { clearSession() }
    }

    override suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResult> {
        return authRemoteDataSource.signUp(email, password, nickname)
            .mapCatching { it.toDomain() }
            .recoverFieldValidationErrors()
    }

    override suspend fun restoreSession(): Boolean {
        val accessToken = tokenStorage.getAccessToken() ?: return false
        TokenHolder.accessToken = accessToken
        return true
    }

    override fun getLoginMethod(): LoginMethod? {
        return tokenStorage.getLoginMethod()?.let { stored ->
            runCatching { LoginMethod.valueOf(stored) }.getOrNull()
        }
    }

    private fun SocialProvider.toLoginMethod(): LoginMethod = when (this) {
        SocialProvider.KAKAO -> LoginMethod.KAKAO
        SocialProvider.GOOGLE -> LoginMethod.GOOGLE
    }

    private fun persistTokens(accessToken: String, refreshToken: String) {
        TokenHolder.accessToken = accessToken
        tokenStorage.saveTokens(accessToken, refreshToken)
    }

    private fun clearSession() {
        TokenHolder.accessToken = null
        tokenStorage.clear()
    }

    private fun <T> Result<T>.recoverFieldValidationErrors(): Result<T> {
        val error = exceptionOrNull() ?: return this
        if (error !is ApiException || error.fieldErrors.isEmpty()) return this

        val fieldErrors = error.fieldErrors.mapNotNull { field ->
            field.field?.let { it to field.message }
        }.toMap()

        return Result.failure(FieldValidationException(fieldErrors, error.message ?: "요청이 올바르지 않습니다"))
    }
}
