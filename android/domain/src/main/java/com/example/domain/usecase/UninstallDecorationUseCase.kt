package com.example.domain.usecase

import com.example.domain.repository.DecorationRepository
import javax.inject.Inject

class UninstallDecorationUseCase @Inject constructor(
    private val decorationRepository: DecorationRepository
) {
    suspend operator fun invoke(decorationId: Long): Result<Unit> =
        decorationRepository.uninstallDecoration(decorationId)
}
