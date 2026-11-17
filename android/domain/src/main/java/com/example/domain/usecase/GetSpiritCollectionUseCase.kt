package com.example.domain.usecase

import com.example.domain.model.SpiritCollectionItem
import com.example.domain.repository.SpiritRepository
import javax.inject.Inject

class GetSpiritCollectionUseCase @Inject constructor(
    private val spiritRepository: SpiritRepository
) {
    suspend operator fun invoke(): Result<List<SpiritCollectionItem>> = spiritRepository.getSpiritCollection()
}
