package com.example.todowithspirits.feature.home.state

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class HomeUiState(
    val spiritInfo: SpiritInfo = SpiritInfo(),
    val selectedDate: LocalDate = LocalDate.now(),
    val todayTasks: List<HomeTodayTask> = emptyList(),
    val isTodayTasksExpanded: Boolean = false
) {
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
    val isImportant: Boolean
)
