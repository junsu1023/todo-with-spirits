package com.example.domain.model

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

data class TaskSummary(
    val category: String,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val endDate: LocalDate?,
    val endTime: LocalTime?,
    val growthType: String?,
    val growthValue: Int,
    val isAllDay: Boolean,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val isPublic: Boolean,
    val memo: String?,
    val notificationAt: LocalDateTime?,
    val notificationMinutes: Int?,
    val occurrenceDate: LocalDate,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: LocalDateTime
)

enum class TaskType(val type: String) {
    SCHEDULE("SCHEDULE"),
    ROUTINE("ROUTINE")
}
