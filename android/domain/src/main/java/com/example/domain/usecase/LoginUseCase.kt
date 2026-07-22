package com.example.domain.usecase

import com.example.domain.model.LoginSession
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginSession> =
        authRepository.login(email, password)
}
