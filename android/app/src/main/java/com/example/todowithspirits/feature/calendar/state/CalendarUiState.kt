package com.example.todowithspirits.feature.calendar.state

import androidx.compose.runtime.Immutable
import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Immutable
data class CalendarUiState(
    val isHidden: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarMonth: YearMonth = YearMonth.now(),
    val calendarEvents: Map<LocalDate, DayPlanEvents> = emptyMap(),
    val sortOption: PlanSortOption = PlanSortOption.DEADLINE,
    val plans: List<PlanItemData> = emptyList(),
    // 펼친 일정 카드. 한 번에 하나만 펼친다.
    val expandedTaskId: Int? = null
) {
    // 완료 제외 중에는 '완료 순' 정렬을 고를 수 없다.
    val sortOptions: List<PlanSortOption>
        get() = PlanSortOption.entries.filter { !(isHidden && it == PlanSortOption.COMPLETE) }

    val visiblePlans: List<PlanItemData>
        get() {
            val filtered = plans.filter { !isHidden || !it.isDone }
            return when (sortOption) {
                PlanSortOption.DEADLINE -> filtered.sortedWith(
                    compareBy<PlanItemData> { it.endDate == null && it.endTime == null && it.isAllDay }
                        .thenBy { it.endDate ?: LocalDate.MAX }
                        .thenBy { it.endTime ?: LocalTime.MAX }
                )
                PlanSortOption.IMPORTANT -> filtered.sortedWith(
                    compareByDescending<PlanItemData> { it.isImportant }.thenBy { it.title }
                )
                PlanSortOption.COMPLETE -> filtered.sortedWith(
                    compareByDescending<PlanItemData> { it.isDone }.thenBy { it.title }
                )
            }
        }
}

// 일정이 있는 날만 맵에 들어간다. (점 표시)
@Immutable
data class DayPlanEvents(
    val importantCount: Int = 0
)
