package com.example.domain.usecase

import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        provider: SocialProvider,
        providerUserId: String,
        providerAccessToken: String,
        email: String? = null
    ): Result<SocialLoginSession> =
        authRepository.socialLogin(provider, providerUserId, providerAccessToken, email)
}
