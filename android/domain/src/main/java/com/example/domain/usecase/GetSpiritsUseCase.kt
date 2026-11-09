package com.example.domain.usecase

import com.example.domain.model.Spirit
import com.example.domain.repository.SpiritRepository
import javax.inject.Inject

class GetSpiritsUseCase @Inject constructor(
    private val spiritRepository: SpiritRepository
) {
    suspend operator fun invoke(): Result<List<Spirit>> = spiritRepository.getSpirits()
}
