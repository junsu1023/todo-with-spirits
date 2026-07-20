package com.example.domain.usecase

import com.example.domain.repository.LoginRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(): Result<Unit> = loginRepository.logout()
}
