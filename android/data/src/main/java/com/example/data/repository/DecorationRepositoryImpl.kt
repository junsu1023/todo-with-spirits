package com.example.data.repository

import com.example.data.datasource.SpiritRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.Decoration
import com.example.domain.model.DecorationPurchase
import com.example.domain.repository.DecorationRepository
import javax.inject.Inject

class DecorationRepositoryImpl @Inject constructor(
    private val spiritRemoteDataSource: SpiritRemoteDataSource
) : DecorationRepository {
    override suspend fun getDecorations(): Result<List<Decoration>> =
        spiritRemoteDataSource.getDecorations().mapCatching { decorations -> decorations.map { it.toDomain() } }

    override suspend fun purchaseDecoration(decorationId: Long): Result<DecorationPurchase> =
        spiritRemoteDataSource.purchaseDecoration(decorationId).mapCatching { it.toDomain() }

    override suspend fun installDecoration(decorationId: Long): Result<Unit> =
        spiritRemoteDataSource.installDecoration(decorationId)

    override suspend fun uninstallDecoration(decorationId: Long): Result<Unit> =
        spiritRemoteDataSource.uninstallDecoration(decorationId)
}
