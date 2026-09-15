package com.example.domain.usecase

import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        nickname: String? = null,
        representativeSpiritId: Long? = null
    ): Result<UserProfile> = authRepository.updateUserProfile(nickname, representativeSpiritId)
}
