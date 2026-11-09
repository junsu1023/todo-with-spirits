package com.example.domain.usecase

import com.example.domain.model.DecorationPurchase
import com.example.domain.repository.DecorationRepository
import javax.inject.Inject

class PurchaseDecorationUseCase @Inject constructor(
    private val decorationRepository: DecorationRepository
) {
    suspend operator fun invoke(decorationId: Long): Result<DecorationPurchase> =
        decorationRepository.purchaseDecoration(decorationId)
}
