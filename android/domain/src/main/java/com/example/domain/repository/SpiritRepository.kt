package com.example.domain.repository

import com.example.domain.model.MetSpirit
import com.example.domain.model.Spirit

interface SpiritRepository {
    suspend fun getSpirits(): Result<List<Spirit>>

    suspend fun getCurrentSpirit(): Result<Spirit>

    suspend fun changeCurrentSpirit(spiritId: Long): Result<Unit>

    suspend fun renameSpirit(spiritId: Long, spiritName: String): Result<Unit>

    // 재화를 소비해 해금된 외형 중 하나를 무작위로 만난다.
    suspend fun meetSpirit(): Result<MetSpirit>
}
