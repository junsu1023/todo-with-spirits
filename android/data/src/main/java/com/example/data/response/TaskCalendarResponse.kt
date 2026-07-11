package com.example.data.response

data class TaskCalendarResponse(
    val completedCount: Int,
    val completedRoutineCount: Int,
    val completedScheduleCount: Int,
    val incompleteCount: Int,
    val routineCount: Int,
    val scheduleCount: Int,
    val totalCount: Int,
    val items: List<TaskListItemResponse> = emptyList()
)

data class TaskListItemResponse(
    val category: String,
    val endDate: String?,
    val endTime: String?,
    val isAllDay: Boolean,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val isPublic: Boolean,
    val memo: String?,
    val notificationMinutes: Int?,
    val repeatEndDate: String?,
    val repeatType: String?,
    val startDate: String,
    val startTime: String?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: String
)