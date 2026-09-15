package com.example.domain.usecase

import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<UserProfile> = authRepository.getUserProfile()
}
