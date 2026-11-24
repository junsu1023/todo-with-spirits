package com.example.todowithspirits.testutil

import com.example.domain.model.MetSpirit
import com.example.domain.model.Spirit
import com.example.domain.model.SpiritCollectionItem
import com.example.domain.repository.SpiritRepository

class FakeSpiritRepository(
    private val collection: List<SpiritCollectionItem> = emptyList()
) : SpiritRepository {
    override suspend fun getSpiritCollection(): Result<List<SpiritCollectionItem>> = Result.success(collection)

    override suspend fun getCurrentSpirit(): Result<Spirit> = Result.failure(UnsupportedOperationException())

    override suspend fun changeCurrentSpirit(spiritId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun renameSpirit(spiritId: Long, spiritName: String): Result<Unit> = Result.success(Unit)

    override suspend fun meetSpirit(): Result<MetSpirit> = Result.failure(UnsupportedOperationException())
}
