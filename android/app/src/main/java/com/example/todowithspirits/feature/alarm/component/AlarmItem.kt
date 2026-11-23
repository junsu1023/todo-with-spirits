package com.example.todowithspirits.feature.alarm.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NotificationCategory
import com.example.domain.model.NotificationItem
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.NotificationDateFormatter
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun AlarmItem(alarm: NotificationItem) {
    val isNew = !alarm.read
    val labelColor = if (isNew) {
        when (alarm.category) {
            NotificationCategory.SYSTEM -> SpiritTodoTheme.color.font02
            NotificationCategory.EVENT -> SpiritTodoTheme.color.key02
            else -> SpiritTodoTheme.color.key01
        }
    } else SpiritTodoTheme.color.fontGrey

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.onSurfaceColor3, RoundedCornerShape(8.dp))
            .border(1.dp, SpiritTodoTheme.color.stroke01, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = alarm.category.displayName,
                fontSize = 12.sp,
                color = labelColor
            )

            Text(
                text = formatNotificationTime(alarm.createdAt),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.fontGrey
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = alarm.content,
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.font01
        )
    }
}

private fun formatNotificationTime(createdAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
    val minutes = ChronoUnit.MINUTES.between(createdAt, now)
    val hours = ChronoUnit.HOURS.between(createdAt, now)
    val days = ChronoUnit.DAYS.between(createdAt, now)

    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 7 -> "${days}일 전"
        else -> createdAt.format(NotificationDateFormatter)
    }
}
