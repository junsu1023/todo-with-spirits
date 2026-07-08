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
    todoFraction: Float,
    routineFraction: Float,
    delayFraction: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(todoFraction)
                .fillMaxHeight()
                .background(SpiritTodoTheme.colors.onSurfaceColor4),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(todoFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.white
            )
        }
        Box(
            modifier = Modifier
                .weight(routineFraction)
                .fillMaxHeight()
                .background(SpiritTodoTheme.colors.onSurfaceColor5),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(routineFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.white
            )
        }
        Box(
            modifier = Modifier
                .weight(delayFraction)
                .fillMaxHeight()
                .background(SpiritTodoTheme.colors.onSurfaceColor11),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(delayFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.white
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LegendDot(color = SpiritTodoTheme.colors.onSurfaceColor4, label = stringResource(R.string.todo))

        Spacer(modifier = Modifier.width(44.dp))

        LegendDot(color = SpiritTodoTheme.colors.onSurfaceColor5, label = stringResource(R.string.routine))

        Spacer(modifier = Modifier.width(44.dp))

        LegendDot(color = SpiritTodoTheme.colors.onSurfaceColor11, label = stringResource(R.string.delay))
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
            color = SpiritTodoTheme.colors.onSurfaceColor7
        )
    }
}