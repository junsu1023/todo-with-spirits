package com.example.data.response

data class TaskCalendarResponse(
    val completedCount: Int,
    val completedRoutineCount: Int,
    val completedScheduleCount: Int,
    val inCompleteCount: Int,
    val routineCount: Int,
    val scheduleCount: Int,
    val totalCount: Int,
    val items: List<TaskListItemResponse> = emptyList()
)

// SCHEDULE 전용(isAllDay, endDate, endTime)과 ROUTINE 전용(excludeHoliday, repeat*) 필드는 nullable.
data class TaskListItemResponse(
    val completedAt: String?,
    val createdAt: String,
    val endDate: String?,
    val endTime: String?,
    val excludeHoliday: Boolean?,
    val growthType: String?,
    val growthValue: Int,
    val isAllDay: Boolean?,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val notificationAt: String?,
    val notificationMinutes: Int?,
    val occurrenceDate: String,
    val repeatDaysOfMonth: List<Int>?,
    val repeatDaysOfWeek: List<String>?,
    val repeatEndDate: String?,
    val repeatType: String?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: String
)
