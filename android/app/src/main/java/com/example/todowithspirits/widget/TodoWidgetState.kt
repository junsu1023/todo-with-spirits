package com.example.todowithspirits.widget

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime

// 홈 화면 위젯(4x2 오늘 일정, 4x4 주간 일정)이 함께 쓰는 상태
@Immutable
sealed interface TodoWidgetState {
    data object Loading : TodoWidgetState

    data object LoggedOut : TodoWidgetState

    // 한 번도 불러오지 못했을 때만 쓴다. 이전에 불러온 일정이 있으면 그대로 보여준다.
    data object Error : TodoWidgetState

    @Immutable
    data class Loaded(
        val today: LocalDate,
        val weekDays: List<WidgetWeekDay>,
        val todayTasks: List<WidgetTask>
    ) : TodoWidgetState {
        val totalCount: Int get() = todayTasks.size
        val completedCount: Int get() = todayTasks.count { it.isCompleted }
        val importantCount: Int get() = todayTasks.count { it.isImportant && !it.isCompleted }
        val isAllCompleted: Boolean get() = totalCount > 0 && completedCount == totalCount
        val progress: Float get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    }
}

@Immutable
data class WidgetWeekDay(
    val date: LocalDate,
    val hasTask: Boolean
)

@Immutable
data class WidgetTask(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val date: LocalDate,
    val time: LocalTime?
)
