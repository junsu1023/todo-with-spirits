package com.example.domain.usecase

import com.example.domain.model.Subscription
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class GetSubscriptionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Subscription> = authRepository.getSubscription()
}
