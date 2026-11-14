package com.example.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

// 루틴 생성/수정 입력. repeatType은 DAILY/WEEKLY/MONTHLY만 허용된다(NONE 불가).
data class NewRoutine(
    val title: String,
    val repeatType: RepeatOption,
    val repeatEndDate: LocalDate? = null,
    val repeatDaysOfWeek: List<DayOfWeek> = emptyList(), // WEEKLY일 때 필수
    val repeatDaysOfMonth: List<Int> = emptyList(), // MONTHLY일 때 필수 (1~31)
    val notificationType: AlarmOption = AlarmOption.NONE,
    val excludeHoliday: Boolean = false
)
