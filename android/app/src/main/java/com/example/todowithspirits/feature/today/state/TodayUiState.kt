package com.example.todowithspirits.feature.today.state

data class TodayUiState(
    val spiritInfo: SpiritInfo
)

data class SpiritInfo(
    val name: String,
    val level: Int,
    val curExp: Int,
    val maxExp: Int,
    val todayPoints: Int
)