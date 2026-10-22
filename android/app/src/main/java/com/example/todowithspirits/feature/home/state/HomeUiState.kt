package com.example.todowithspirits.feature.home.state

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class HomeUiState(
    val spiritInfo: SpiritInfo = SpiritInfo(),
    val selectedDate: LocalDate = LocalDate.now(),
    val todayTasks: List<HomeTodayTask> = emptyList(),
    val isTodayTasksExpanded: Boolean = false,
    val selectedTaskId: Long? = null
) {
    // 상세 팝업에 띄울 일정. null이면 팝업을 닫는다.
    val selectedTask: HomeTodayTask? get() = selectedTaskId?.let { id -> todayTasks.find { it.id == id } }

    val todayTotalCount: Int get() = todayTasks.size
    val todayCompletedCount: Int get() = todayTasks.count { it.isCompleted }
    val todayImportantCount: Int get() = todayTasks.count { it.isImportant && !it.isCompleted }
}

@Immutable
data class SpiritInfo(
    val name: String = ""
)

// 홈 '오늘 일정' 카드에 펼쳐지는 항목
@Immutable
data class HomeTodayTask(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val date: LocalDate,
    val time: LocalTime? = null
)
