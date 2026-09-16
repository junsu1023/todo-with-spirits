package com.example.domain.usecase

import com.example.domain.model.EmailAvailability
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class CheckEmailAvailabilityUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<EmailAvailability> = authRepository.checkEmail(email)
}
