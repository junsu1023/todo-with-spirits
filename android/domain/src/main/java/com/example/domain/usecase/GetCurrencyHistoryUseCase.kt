package com.example.domain.usecase

import com.example.domain.model.CurrencyHistory
import com.example.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetCurrencyHistoryUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(): Result<CurrencyHistory> = currencyRepository.getCurrencyHistory()
}
