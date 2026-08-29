package com.example.domain.repository

import com.example.domain.model.DailyRecord

interface RecordRepository {
    suspend fun getTodayRecord(): Result<DailyRecord>
}
