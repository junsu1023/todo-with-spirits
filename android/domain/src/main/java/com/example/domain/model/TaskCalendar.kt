package com.example.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TaskCalendar(
    val completedCount: Int,
    val completedRoutineCount: Int,
    val completedScheduleCount: Int,
    val incompleteCount: Int,
    val routineCount: Int,
    val scheduleCount: Int,
    val totalCount: Int,
    val items: List<TaskSummary>
)

data class TaskSummary(
    val category: String,
    val endDate: LocalDate?,
    val endTime: LocalTime?,
    val isAllDay: Boolean,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val isPublic: Boolean,
    val memo: String?,
    val notificationMinutes: Int?,
    val repeatDaysOfWeek: List<String>,
    val repeatDaysOfMonth: List<Int>,
    val repeatEndDate: LocalDate?,
    val repeatType: String?,
    val startDate: LocalDate,
    val startTime: LocalTime?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: LocalDateTime
)

enum class TaskType(val type: String) {
    SCHEDULE("SCHEDULE"),
    ROUTINE("ROUTINE")
}