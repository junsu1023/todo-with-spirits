package com.example.domain.usecase

import com.example.domain.model.DailyRecord
import com.example.domain.repository.RecordRepository
import javax.inject.Inject

class GetDailyRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    suspend operator fun invoke(): Result<DailyRecord> = recordRepository.getTodayRecord()
}
