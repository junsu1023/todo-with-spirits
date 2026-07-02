package com.example.domain.usecase

import com.example.domain.repository.HealthCheckRepository
import javax.inject.Inject

class CheckSystemHealthUseCase @Inject constructor(
    private val healthCheckRepository: HealthCheckRepository
) {
    suspend operator fun invoke(): String {
        return if(healthCheckRepository.checkSystemHealth().isSuccess) "Connected" else "Not Connected"
    }
}