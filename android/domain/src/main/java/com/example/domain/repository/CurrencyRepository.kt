package com.example.domain.repository

import com.example.domain.model.CurrencyHistory

interface CurrencyRepository {
    suspend fun getCurrencyHistory(): Result<CurrencyHistory>
}
