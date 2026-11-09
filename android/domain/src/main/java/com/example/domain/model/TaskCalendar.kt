package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TaskCalendar(
    val completedCount: Int,
    val completedRoutineCount: Int,
    val completedScheduleCount: Int,
    val inCompleteCount: Int,
    val routineCount: Int,
    val scheduleCount: Int,
    val totalCount: Int,
    val items: List<TaskSummary>
)

// SCHEDULE 전용: isAllDay, endDate, endTime / ROUTINE 전용: excludeHoliday, repeat*
data class TaskSummary(
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val endDate: LocalDate?,
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
    val taskType: String,
    val title: String,
    val updatedAt: LocalDateTime
)

enum class TaskType(val type: String) {
    SCHEDULE("SCHEDULE"),
    ROUTINE("ROUTINE")
}
