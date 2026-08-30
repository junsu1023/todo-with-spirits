package com.example.data.repository

import com.example.data.datasource.RecordRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.DailyRecord
import com.example.domain.model.WeeklyRecord
import com.example.domain.repository.RecordRepository
import java.time.LocalDate
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val recordRemoteDataSource: RecordRemoteDataSource
) : RecordRepository {
    override suspend fun getTodayRecord(): Result<DailyRecord> =
        recordRemoteDataSource.getTodayRecord().mapCatching { it.toDomain() }

    override suspend fun getWeeklyRecord(date: LocalDate): Result<WeeklyRecord> =
        recordRemoteDataSource.getWeeklyRecord(date.toString()).mapCatching { it.toDomain() }
}
