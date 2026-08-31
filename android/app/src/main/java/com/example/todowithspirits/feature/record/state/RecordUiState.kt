package com.example.todowithspirits.feature.record.state

import androidx.compose.runtime.Immutable
import com.example.domain.model.DailyRecord
import com.example.domain.model.WeeklyRecord

@Immutable
data class RecordUiState(
    val dailyRecord: DailyRecord? = null,
    val weeklyRecord: WeeklyRecord? = null
)
