package com.example.data.repository

import com.example.data.datasource.SpiritRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.CurrencyHistory
import com.example.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val spiritRemoteDataSource: SpiritRemoteDataSource
) : CurrencyRepository {
    override suspend fun getCurrencyHistory(): Result<CurrencyHistory> =
        spiritRemoteDataSource.getCurrencyHistory().mapCatching { it.toDomain() }
}
