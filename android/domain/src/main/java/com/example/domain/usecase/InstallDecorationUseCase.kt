package com.example.domain.usecase

import com.example.domain.repository.DecorationRepository
import javax.inject.Inject

class InstallDecorationUseCase @Inject constructor(
    private val decorationRepository: DecorationRepository
) {
    suspend operator fun invoke(decorationId: Long): Result<Unit> =
        decorationRepository.installDecoration(decorationId)
}
