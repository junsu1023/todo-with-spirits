package com.example.data.repository

import com.example.data.datasource.SpiritRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.MetSpirit
import com.example.domain.model.Spirit
import com.example.domain.repository.SpiritRepository
import javax.inject.Inject

class SpiritRepositoryImpl @Inject constructor(
    private val spiritRemoteDataSource: SpiritRemoteDataSource
) : SpiritRepository {
    override suspend fun getSpirits(): Result<List<Spirit>> =
        spiritRemoteDataSource.getSpirits().mapCatching { spirits -> spirits.map { it.toDomain() } }

    override suspend fun getCurrentSpirit(): Result<Spirit> =
        spiritRemoteDataSource.getCurrentSpirit().mapCatching { it.toDomain(defaultRepresentative = true) }

    override suspend fun changeCurrentSpirit(spiritId: Long): Result<Unit> =
        spiritRemoteDataSource.changeCurrentSpirit(spiritId)

    override suspend fun renameSpirit(spiritId: Long, spiritName: String): Result<Unit> =
        spiritRemoteDataSource.renameSpirit(spiritId, spiritName)

    override suspend fun meetSpirit(): Result<MetSpirit> =
        spiritRemoteDataSource.meetSpirit().mapCatching { it.toDomain() }
}
