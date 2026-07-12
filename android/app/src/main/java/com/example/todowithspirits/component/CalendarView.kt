package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

private data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    showMonthNavigation: Boolean = true
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    val calendarDays = remember(currentMonth) {
        val firstDayOfMonth = currentMonth.atDay(1)
        val leadingCount = firstDayOfMonth.dayOfWeek.value % 7

        val days = mutableListOf<CalendarDay>()
        for (offset in leadingCount downTo 1) {
            days.add(CalendarDay(firstDayOfMonth.minusDays(offset.toLong()), isCurrentMonth = false))
        }

        for (day in 1..currentMonth.lengthOfMonth()) {
            days.add(CalendarDay(currentMonth.atDay(day), isCurrentMonth = true))
        }

        val lastDayOfMonth = currentMonth.atEndOfMonth()
        val trailingCount = (7 - days.size % 7) % 7
        for (offset in 1..trailingCount) {
            days.add(CalendarDay(lastDayOfMonth.plusDays(offset.toLong()), isCurrentMonth = false))
        }

        days
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (showMonthNavigation) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.month, currentMonth.monthValue),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiritTodoTheme.color.onSurfaceColor1
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.left),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = { currentMonth = currentMonth.minusMonths(1) })
                    )

                    Image(
                        painter = painterResource(R.drawable.right),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = { currentMonth = currentMonth.plusMonths(1) })
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 18.dp)
        ) {
            val weekDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor9
                )
            }
        }

        val chunks = calendarDays.chunked(7)
        chunks.forEachIndexed { weekIdx, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { calendarDay ->
                    val date = calendarDay.date
                    val isSelected = date == selectedDate

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                onClick = {
                                    onDateSelected(date)
                                    val month = YearMonth.from(date)
                                    if (month != currentMonth) currentMonth = month
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SpiritTodoTheme.color.surfaceColor3, CircleShape)
                            )
                        }

                        Text(
                            text = date.dayOfMonth.toString(),
                            color = when {
                                isSelected -> SpiritTodoTheme.color.onSurfaceColor3
                                !calendarDay.isCurrentMonth -> SpiritTodoTheme.color.onSurfaceColor9
                                else -> SpiritTodoTheme.color.onSurfaceColor1
                            },
                            fontSize = 12.sp,
                            fontWeight = if(isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            if (weekIdx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
