package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.YearMonth

@Composable
fun MonthlyCalendarView(
    modifier: Modifier = Modifier,
    selectedDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    compact: Boolean = false
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = remember(currentMonth) {
        val days = mutableListOf<Int?>()
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekOfFirstDay = firstDayOfMonth.dayOfWeek.value % 7
        repeat(dayOfWeekOfFirstDay) { days.add(null) }
        for (day in 1..currentMonth.lengthOfMonth()) {
            days.add(day)
        }
        days
    }

    val cellSize = if (compact) 22.dp else 32.dp
    val weekSpacing = if (compact) 0.dp else 18.dp
    val headerHeight = if (compact) 24.dp else 36.dp
    val rowHeight = if (compact) 28.dp else 32.dp

    Column(modifier = modifier.fillMaxWidth()) {
        if (!compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.month, currentMonth.monthValue),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.left),
                        contentDescription = null,
                        modifier = Modifier.clickable { currentMonth = currentMonth.minusMonths(1) }
                    )
                    Image(
                        painter = painterResource(R.drawable.right),
                        contentDescription = null,
                        modifier = Modifier.clickable { currentMonth = currentMonth.plusMonths(1) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontSize = if (compact) 10.sp else 12.sp,
                    color = SpiritTodoTheme.colors.textColor1
                )
            }
        }

        val chunks = daysInMonth.chunked(7)
        chunks.forEachIndexed { weekIdx, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { day ->
                    val isSelected = day != null && day in selectedDays

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = day != null,
                                onClick = { day?.let { onDayToggled(it) } },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .background(SpiritTodoTheme.colors.surfaceColor3, CircleShape)
                                )
                            }

                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else SpiritTodoTheme.colors.mainTextColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            if (weekIdx != chunks.lastIndex && !compact) {
                Spacer(modifier = Modifier.height(weekSpacing))
            }
        }
    }
}
