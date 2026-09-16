package com.example.todowithspirits.feature.alarm.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NotificationItem
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun AlarmItem(alarm: NotificationItem) {
    val isNew = !alarm.read
    val labelColor = if (isNew) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.systemGrey

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(SpiritTodoTheme.color.onSurfaceColor3, RoundedCornerShape(8.dp))
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = alarm.category.displayName,
                fontSize = 12.sp,
                color = labelColor
            )

            Text(
                text = formatNotificationTime(alarm.createdAt),
                fontSize = 12.sp,
                color = labelColor
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = alarm.content,
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}

private val notificationDateFormatter = DateTimeFormatter.ofPattern("MM. dd")

private fun formatNotificationTime(createdAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
    val minutes = ChronoUnit.MINUTES.between(createdAt, now)
    val hours = ChronoUnit.HOURS.between(createdAt, now)
    val days = ChronoUnit.DAYS.between(createdAt, now)

    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 7 -> "${days}일 전"
        else -> createdAt.format(notificationDateFormatter)
    }
}
