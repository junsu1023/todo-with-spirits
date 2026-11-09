package com.example.domain.usecase

import com.example.domain.repository.SpiritRepository
import javax.inject.Inject

class ChangeCurrentSpiritUseCase @Inject constructor(
    private val spiritRepository: SpiritRepository
) {
    suspend operator fun invoke(spiritId: Long): Result<Unit> = spiritRepository.changeCurrentSpirit(spiritId)
}
