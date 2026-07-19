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

data class TaskListItemResponse(
    val category: String,
    val completedAt: String?,
    val createdAt: String,
    val endDate: String?,
    val endTime: String?,
    val growthType: String?,
    val growthValue: Int,
    val isAllDay: Boolean,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val isPublic: Boolean,
    val memo: String?,
    val notificationAt: String?,
    val notificationMinutes: Int?,
    val occurrenceDate: String,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: String
)
