package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Task(
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
    val memo: String,
    val notificationMinutes: Int?,
    val repeatDaysOfMonth: List<Int>,
    val repeatDaysOfWeek: List<DayOfWeek>,
    val repeatEndDate: LocalDate?,
    val repeatType: String?,
    val startDate: LocalDate?,
    val startTime: LocalTime?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: LocalDateTime
)
