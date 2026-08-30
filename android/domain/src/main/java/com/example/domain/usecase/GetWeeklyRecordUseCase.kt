package com.example.domain.usecase

import com.example.domain.model.WeeklyRecord
import com.example.domain.repository.RecordRepository
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): Result<WeeklyRecord> =
        recordRepository.getWeeklyRecord(date)
}
