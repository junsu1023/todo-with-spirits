package com.example.todowithspirits.feature.add.state

import com.example.domain.model.AlarmOption
import com.example.domain.model.CategoryOption
import com.example.domain.model.PublicStateOption
import com.example.domain.model.RepeatOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class AddUiState(
    val title: String = "",
    val isImportant: Boolean = false,

    // Todo 전용 (Todo 생성 API 연동 전까지는 값만 보관)
    val date: LocalDate = LocalDate.now(),
    val isTimeEnabled: Boolean = false,
    val dueTime: LocalTime = LocalTime.of(0, 0),

    // 루틴 전용
    val repeatOption: RepeatOption = RepeatOption.DAILY,
    val selectedWeekDays: Set<DayOfWeek> = emptySet(),
    val selectedMonthDays: Set<Int> = emptySet(),
    val excludeHolidays: Boolean = false,

    // Todo/루틴 공통
    val alarmOption: AlarmOption = AlarmOption.NONE,
    val categoryOption: CategoryOption = CategoryOption.NONE,
    val publicOption: PublicStateOption = PublicStateOption.PRIVATE,
    val memo: String = ""
)
