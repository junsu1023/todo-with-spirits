package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TaskCalendar(
    val completedCount: Int,
    val inCompleteCount: Int,
    val totalCount: Int,
    val items: List<TaskSummary>
)

// 특정 날짜(occurrenceDate)의 일정 1건. repeatType이 NONE이면 단발성, 그 외는 반복 일정이다.
data class TaskSummary(
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val endTime: LocalTime?,
    val excludeHoliday: Boolean,
    val growthType: String?,
    val growthValue: Int,
    val isAllDay: Boolean,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val notificationAt: LocalDateTime?,
    val notificationMinutes: Int?,
    val occurrenceDate: LocalDate,
    val repeatDaysOfMonth: List<Int>,
    val repeatDaysOfWeek: List<DayOfWeek>,
    val repeatEndDate: LocalDate?,
    val repeatType: String?,
    val taskId: Long,
    val title: String,
    val updatedAt: LocalDateTime
)
