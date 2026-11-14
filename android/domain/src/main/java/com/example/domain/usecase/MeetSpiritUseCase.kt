package com.example.domain.usecase

import com.example.domain.model.MetSpirit
import com.example.domain.repository.SpiritRepository
import javax.inject.Inject

class MeetSpiritUseCase @Inject constructor(
    private val spiritRepository: SpiritRepository
) {
    suspend operator fun invoke(): Result<MetSpirit> = spiritRepository.meetSpirit()
}
