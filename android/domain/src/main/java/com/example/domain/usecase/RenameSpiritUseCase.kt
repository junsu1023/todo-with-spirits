package com.example.domain.usecase

import com.example.domain.repository.SpiritRepository
import javax.inject.Inject

class RenameSpiritUseCase @Inject constructor(
    private val spiritRepository: SpiritRepository
) {
    suspend operator fun invoke(spiritId: Long, spiritName: String): Result<Unit> =
        spiritRepository.renameSpirit(spiritId, spiritName)
}
