package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class NewRoutine(
    val title: String,
    val repeatType: RepeatOption,
    val category: CategoryOption = CategoryOption.NONE,
    val repeatEndDate: LocalDate? = null,
    val repeatDaysOfWeek: List<DayOfWeek> = emptyList(),
    val repeatDaysOfMonth: List<Int> = emptyList(),
    val notification: AlarmOption = AlarmOption.NONE,
    val isPublic: Boolean = false,
    val excludeHoliday: Boolean = false,
    val memo: String? = null
)
