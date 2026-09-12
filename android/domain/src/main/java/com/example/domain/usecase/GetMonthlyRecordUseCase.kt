package com.example.domain.usecase

import com.example.domain.model.MonthlyRecord
import com.example.domain.repository.RecordRepository
import java.time.LocalDate
import javax.inject.Inject

class GetMonthlyRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): Result<MonthlyRecord> =
        recordRepository.getMonthlyRecord(date)
}
