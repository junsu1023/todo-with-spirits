package com.example.todowithspirits.feature.plan.state

import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.feature.plan.PlanItemData
import com.example.todowithspirits.feature.plan.PlanType
import java.time.LocalDate
import java.time.YearMonth

data class PlanUiState(
    val selectedTab: String = "전체",
    val isHidden: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarMonth: YearMonth = YearMonth.now(),
    val calendarEvents: Map<LocalDate, DayPlanEvents> = emptyMap(),
    val sortOption: PlanSortOption = PlanSortOption.fromDisplayName("마감 임박 순"),
    val plans: List<PlanItemData> = emptyList()
)

data class DayPlanEvents(
    val types: List<PlanType> = emptyList(),
    val importantCount: Int = 0
)
