package com.example.todowithspirits.util

import com.example.domain.model.RepeatOption
import com.example.domain.model.TaskSummary
import java.time.LocalDate

fun TaskSummary.routineOccurrences(from: LocalDate, to: LocalDate): List<LocalDate> {
    val start = maxOf(from, startDate)
    val end = repeatEndDate?.let { minOf(to, it) } ?: to
    if (start.isAfter(end)) return emptyList()

    return generateSequence(start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(end) }
        .filter { date ->
            when (repeatType) {
                RepeatOption.DAILY.name -> true
                RepeatOption.WEEKLY.name -> date.dayOfWeek.name in repeatDaysOfWeek
                RepeatOption.MONTHLY.name -> date.dayOfMonth in repeatDaysOfMonth
                RepeatOption.YEARLY.name -> date.month == startDate.month && date.dayOfMonth == startDate.dayOfMonth
                else -> date == startDate
            }
        }
        .toList()
}
