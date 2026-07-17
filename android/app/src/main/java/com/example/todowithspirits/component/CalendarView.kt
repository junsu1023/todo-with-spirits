package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

private data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

data class CalendarDayEvent(
    val dotColors: List<Color> = emptyList(),
    val label: String? = null
)

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    showMonthNavigation: Boolean = true,
    showSelectedDateInHeader: Boolean = false,
    eventData: Map<LocalDate, CalendarDayEvent> = emptyMap(),
    onMonthChanged: (YearMonth) -> Unit = {}
) {
    var currentMonth by remember(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    val headerDate = remember(currentMonth, selectedDate) {
        currentMonth.atDay(selectedDate.dayOfMonth.coerceAtMost(currentMonth.lengthOfMonth()))
    }
    val selectedDateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM.", Locale.KOREAN) }

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
                    text = if (showSelectedDateInHeader) {
                        headerDate.format(selectedDateFormatter)
                    } else {
                        stringResource(R.string.month, currentMonth.monthValue)
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.todoTextMain
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.left),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            currentMonth = currentMonth.minusMonths(1)
                            onMonthChanged(currentMonth)
                        })
                    )

                    Image(
                        painter = painterResource(R.drawable.right),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            currentMonth = currentMonth.plusMonths(1)
                            onMonthChanged(currentMonth)
                        })
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
                    .height(IntrinsicSize.Max)
            ) {
                week.forEach { calendarDay ->
                    val date = calendarDay.date
                    val isSelected = date == selectedDate
                    val event = eventData[date]

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                onClick = {
                                    onDateSelected(date)
                                    val month = YearMonth.from(date)
                                    if (month != currentMonth) {
                                        currentMonth = month
                                        onMonthChanged(month)
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(SpiritTodoTheme.color.mainArea, CircleShape)
                                )
                            }

                            Text(
                                text = date.dayOfMonth.toString(),
                                color = when {
                                    isSelected -> SpiritTodoTheme.color.onSurfaceColor3
                                    !calendarDay.isCurrentMonth -> SpiritTodoTheme.color.onSurfaceColor9
                                    else -> SpiritTodoTheme.color.todoTextMain
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        if (event != null && event.dotColors.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                event.dotColors.forEach { dotColor ->
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(dotColor, CircleShape)
                                    )
                                }
                            }
                        }

                        if (event?.label != null) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(SpiritTodoTheme.color.surfaceColor14, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = event.label,
                                    fontSize = 10.sp,
                                    color = SpiritTodoTheme.color.todoTextMain,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            if (weekIdx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
