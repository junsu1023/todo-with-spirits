package com.example.domain.usecase

import com.example.domain.model.Decoration
import com.example.domain.repository.DecorationRepository
import javax.inject.Inject

class GetDecorationsUseCase @Inject constructor(
    private val decorationRepository: DecorationRepository
) {
    suspend operator fun invoke(): Result<List<Decoration>> = decorationRepository.getDecorations()
}
