package com.example.domain.usecase

import com.example.domain.repository.CheckSystemHealthRepository
import javax.inject.Inject

class CheckSystemHealthUseCase @Inject constructor(
    private val checkSystemHealthRepository: CheckSystemHealthRepository
) {
    suspend operator fun invoke(): String {
        return if(checkSystemHealthRepository.checkSystemHealth().isSuccess) "Connected" else "Not Connected"
    }
}