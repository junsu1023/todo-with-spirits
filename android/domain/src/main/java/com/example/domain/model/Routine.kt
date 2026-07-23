package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

data class Routine(
    val category: String,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val excludeHoliday: Boolean,
    val growthType: String?,
    val growthValue: Int,
    val isCompleted: Boolean,
    val isPublic: Boolean,
    val memo: String,
    val notificationAt: LocalDateTime?,
    val notificationMinutes: Int?,
    val repeatDaysOfMonth: List<Int>,
    val repeatDaysOfWeek: List<DayOfWeek>,
    val repeatEndDate: LocalDate?,
    val repeatType: String,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: LocalDateTime
)
