package com.example.todowithspirits.feature.record.state

import androidx.compose.runtime.Immutable
import com.example.domain.model.DailyRecord

@Immutable
data class RecordUiState(
    val dailyRecord: DailyRecord? = null
)
