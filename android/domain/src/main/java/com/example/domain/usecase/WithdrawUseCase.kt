package com.example.domain.usecase

import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class WithdrawUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.withdraw()
}
