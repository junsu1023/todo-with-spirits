package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// SCHEDULE 전용: isAllDay, endDate, endTime / ROUTINE 전용: excludeHoliday, repeat*
data class Task(
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
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
    val occurrenceDate: LocalDate?,
    val repeatDaysOfMonth: List<Int>,
    val repeatDaysOfWeek: List<DayOfWeek>,
    val repeatEndDate: LocalDate?,
    val repeatType: String?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: LocalDateTime
)

data class TaskList(
    val totalCount: Int,
    val items: List<Task>
)

// 일정/루틴 완료 시 지급된 재화
data class TaskCompletionReward(
    val currencyEarned: Int,
    val balance: Int
)
