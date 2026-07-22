package com.example.domain.usecase

import com.example.domain.model.SignUpResult
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, nickname: String?): Result<SignUpResult> =
        authRepository.signUp(email, password, nickname)
}
