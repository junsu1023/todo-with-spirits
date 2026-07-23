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
import com.example.todowithspirits.feature.alarm.AlarmData
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AlarmItem(alarm: AlarmData, isNew: Boolean) {
    val labelColor = if(isNew) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.systemGrey

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
                text = alarm.type,
                fontSize = 12.sp,
                color = labelColor
            )

            Text(
                text = alarm.timeLabel,
                fontSize = 12.sp,
                color = labelColor
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = alarm.message,
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}