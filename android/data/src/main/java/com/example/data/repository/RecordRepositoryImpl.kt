package com.example.data.repository

import com.example.data.datasource.RecordRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.DailyRecord
import com.example.domain.repository.RecordRepository
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val recordRemoteDataSource: RecordRemoteDataSource
) : RecordRepository {
    override suspend fun getTodayRecord(): Result<DailyRecord> =
        recordRemoteDataSource.getTodayRecord().mapCatching { it.toDomain() }
}
