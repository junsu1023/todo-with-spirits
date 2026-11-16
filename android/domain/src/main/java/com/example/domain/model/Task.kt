package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// 일정 단건. repeatType이 NONE이면 단발성, DAILY/WEEKLY/MONTHLY면 반복 일정이다.
data class Task(
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
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
    // 단건 조회 응답에는 없고 생성·수정 응답에만 있다. (단발성: 날짜 / 반복: 반복 시작일)
    val startDate: LocalDate?,
    val taskId: Long,
    val title: String,
    val updatedAt: LocalDateTime
)

// 일정 완료 시 지급된 재화
data class TaskCompletionReward(
    val currencyEarned: Int,
    val balance: Int
)
