package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun WeeklyAnalysisSection(
    todoFraction: Double,
    routineFraction: Double,
    delayFraction: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(if(todoFraction == 0.0) 1f else (todoFraction / 100).toFloat())
                .fillMaxHeight()
                .background(SpiritTodoTheme.color.keyTodo),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(todoFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.onSurfaceColor3
            )
        }
        Box(
            modifier = Modifier
                .weight(if(routineFraction == 0.0) 1f else (routineFraction / 100).toFloat())
                .fillMaxHeight()
                .background(SpiritTodoTheme.color.keyRoutine),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(routineFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.onSurfaceColor3
            )
        }
        Box(
            modifier = Modifier
                .weight(if(delayFraction == 0.0) 1f else (delayFraction / 100f).toFloat())
                .fillMaxHeight()
                .background(SpiritTodoTheme.color.surfaceColor15),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(delayFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.onSurfaceColor3
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LegendDot(color = SpiritTodoTheme.color.keyTodo, label = stringResource(R.string.todo))

        Spacer(modifier = Modifier.width(44.dp))

        LegendDot(color = SpiritTodoTheme.color.keyRoutine, label = stringResource(R.string.routine))

        Spacer(modifier = Modifier.width(44.dp))

        LegendDot(color = SpiritTodoTheme.color.surfaceColor15, label = stringResource(R.string.delay))
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(color, CircleShape))

        Spacer(Modifier.width(6.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.onSurfaceColor8
        )
    }
}