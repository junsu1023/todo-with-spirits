package com.example.todowithspirits.feature.plan.state

import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.feature.plan.PlanItemData
import java.time.LocalDate

data class PlanUiState(
    val selectedTab: String = "전체",
    val isHidden: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val sortOption: PlanSortOption = PlanSortOption.fromDisplayName("마감 임박 순"),
    val plans: List<PlanItemData> = emptyList()
)