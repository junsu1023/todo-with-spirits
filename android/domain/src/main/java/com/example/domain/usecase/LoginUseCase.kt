package com.example.domain.usecase

import com.example.domain.model.LoginSession
import com.example.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginSession> =
        loginRepository.login(email, password)
}