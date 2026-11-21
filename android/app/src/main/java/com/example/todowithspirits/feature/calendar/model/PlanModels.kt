package com.example.todowithspirits.feature.calendar.model

import androidx.annotation.StringRes
import com.example.domain.model.PlanSortOption
import com.example.domain.model.TaskSummary
import com.example.todowithspirits.R
import java.time.LocalDate
import java.time.LocalTime

data class PlanItemData(
    val id: Int,
    val title: String,
    val isImportant: Boolean,
    val isDone: Boolean,
    val endDate: LocalDate?,
    val endTime: LocalTime?,
    val isAllDay: Boolean = false
)

fun TaskSummary.toPlanItemData(): PlanItemData = PlanItemData(
    id = taskId.toInt(),
    title = title,
    isImportant = isImportant,
    isDone = isCompleted,
    endDate = occurrenceDate,
    endTime = endTime,
    isAllDay = isAllDay
)

@get:StringRes
val PlanSortOption.labelRes: Int
    get() = when (this) {
        PlanSortOption.DEADLINE -> R.string.sort_by_time
        PlanSortOption.IMPORTANT -> R.string.sort_by_important
        PlanSortOption.COMPLETE -> R.string.sort_by_complete
    }
