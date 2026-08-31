package com.example.domain.repository

import com.example.domain.model.DailyRecord
import com.example.domain.model.WeeklyRecord
import java.time.LocalDate

interface RecordRepository {
    suspend fun getTodayRecord(): Result<DailyRecord>

    suspend fun getWeeklyRecord(date: LocalDate): Result<WeeklyRecord>
}
