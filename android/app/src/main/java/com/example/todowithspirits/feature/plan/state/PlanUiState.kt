package com.example.todowithspirits.feature.plan.state

import java.time.LocalDate

data class PlanUiState(
    val selectedTab: String = "전체",
    val isHidden: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now()
)