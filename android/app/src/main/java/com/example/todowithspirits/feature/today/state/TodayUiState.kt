package com.example.todowithspirits.feature.today.state

data class TodayUiState(
    val spiritInfo: SpiritInfo
)

data class SpiritInfo(
    val name: String = "",
    val level: Int = 0,
    val curExp: Int = 0,
    val maxExp: Int = 0,
    val todayPoints: Int = 0
)