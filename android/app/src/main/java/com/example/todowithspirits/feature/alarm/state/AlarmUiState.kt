package com.example.todowithspirits.feature.alarm.state

import androidx.compose.runtime.Immutable
import com.example.domain.model.NotificationItem

@Immutable
data class AlarmUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val nextCursor: String? = null,
    val hasNext: Boolean = false
) {
    val newAlarms: List<NotificationItem>
        get() = notifications.filter { !it.read }

    val pastAlarms: List<NotificationItem>
        get() = notifications.filter { it.read }
}
