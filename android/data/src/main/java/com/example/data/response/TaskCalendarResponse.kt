package com.example.data.response

// 일정 목록 조회 응답 (GET /api/task). 반복 일정은 날짜마다 한 건씩 펼쳐져 내려온다.
data class TaskCalendarResponse(
    val completedCount: Int,
    val inCompleteCount: Int,
    val totalCount: Int,
    val items: List<TaskListItemResponse> = emptyList()
)

// 특정 날짜의 일정 1건. 단발성/반복 공통이며 repeatType이 NONE이면 단발성이다.
data class TaskListItemResponse(
    val completedAt: String?,
    val createdAt: String,
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
    val title: String,
    val updatedAt: String
)
