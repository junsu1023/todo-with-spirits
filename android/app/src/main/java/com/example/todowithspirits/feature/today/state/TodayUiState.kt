package com.example.todowithspirits.feature.today.state

import androidx.compose.runtime.Immutable
import com.example.todowithspirits.feature.plan.PlanType
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class TodayUiState(
    val spiritInfo: SpiritInfo = SpiritInfo(),
    val selectedDate: LocalDate = LocalDate.now(),
    val todos: List<TodoItem> = emptyList(),
    val routines: List<RoutineItem> = emptyList(),
    val weekEvents: Map<LocalDate, List<PlanType>> = emptyMap()
)

@Immutable
data class SpiritInfo(
    val name: String = "",
    val level: Int = 0,
    val curExp: Int = 0,
    val maxExp: Int = 0,
    val todayPoints: Int = 0
)

@Immutable
data class TodoItem(
    val taskId: Long,
    val title: String,
    val isDone: Boolean,
    val isImportant: Boolean,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val memo: String = ""
)

@Immutable
data class RoutineItem(
    val taskId: Long,
    val title: String,
    val isDone: Boolean,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val memo: String = ""
)
