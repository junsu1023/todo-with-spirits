package com.example.todowithspirits.component.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventLabels: Map<LocalDate, String?> = emptyMap(),
    onMonthChanged: (YearMonth) -> Unit = {},
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
) {
    CalendarPager(
        selectedDate = selectedDate,
        onMonthChanged = onMonthChanged,
        modifier = modifier
            .background(SpiritTodoTheme.color.white, RoundedCornerShape(8.dp))
            .border(1.dp, SpiritTodoTheme.color.stroke01, RoundedCornerShape(8.dp)),
        header = { month, onPrevious, onNext ->
            Spacer(modifier = Modifier.height(14.dp))

            CalendarHeader(
                year = month.year,
                month = month.monthValue,
                onNext = onNext,
                onPrevious = onPrevious
            )

            Spacer(modifier = Modifier.height(4.dp))

            CalendarWeekHeader(firstDayOfWeek)

            Spacer(modifier = Modifier.height(6.dp))
        }
    ) { month ->
        CalendarMonthGrid(
            month = month,
            selectedDate = selectedDate,
            eventLabels = eventLabels,
            onDateSelected = onDateSelected,
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        )
    }
}

@Composable
private fun CalendarHeader(
    year: Int,
    month: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.year_month, year, month),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.font01
        )

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(R.drawable.widgetly_left_20),
                contentDescription = null,
                modifier = Modifier.clickable(onClick = onPrevious)
            )

            Image(
                painter = painterResource(R.drawable.widgetly_right_20),
                contentDescription = null,
                modifier = Modifier.clickable(onClick = onNext)
            )
        }
    }
}

@Composable
private fun CalendarWeekHeader(firstDayOfWeek: DayOfWeek) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weekDays = remember(firstDayOfWeek) { weekDayLabels(firstDayOfWeek) }

        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font02
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    eventLabels: Map<LocalDate, String?>,
    onDateSelected: (LocalDate) -> Unit,
    firstDayOfWeek: DayOfWeek,
    modifier: Modifier = Modifier
) {
    val calendarDays = remember(month, firstDayOfWeek) { calendarDaysOf(month, firstDayOfWeek) }

    Column(modifier = modifier.fillMaxWidth()) {
        val chunks = calendarDays.chunked(7)
        chunks.forEachIndexed { weekIdx, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                week.forEach { calendarDay ->
                    val date = calendarDay.date
                    val isSelected = date == selectedDate
                    val hasEvent = date in eventLabels
                    val label = eventLabels[date]

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .noRippleClickable { onDateSelected(date) },
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DayArea(
                            isSelected = isSelected,
                            isCurrentMonth = calendarDay.isCurrentMonth,
                            day = date.dayOfMonth.toString()
                        )

                        if (hasEvent) {
                            DotArea()
                        }

                        if (label != null) {
                            LabelArea(label)
                        }
                    }
                }
            }

            if (weekIdx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun DayArea(
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    day: String
) {
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .requiredSize(14.dp)
                    .background(SpiritTodoTheme.color.key01, CircleShape)
            )
        }

        Text(
            text = day,
            color = when {
                isSelected -> SpiritTodoTheme.color.onSurfaceColor3
                !isCurrentMonth -> SpiritTodoTheme.color.surfaceColor15
                else -> SpiritTodoTheme.color.todoTextMain
            },
            fontSize = when {
                isSelected -> 10.sp
                else -> 12.sp
            },
            fontWeight = FontWeight.Medium,
            softWrap = false
        )
    }
}

@Composable
private fun DotArea() {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(SpiritTodoTheme.color.key02, CircleShape)
        )
    }
}

@Composable
private fun LabelArea(label: String) {
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .background(SpiritTodoTheme.color.key02Op, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = SpiritTodoTheme.color.font01,
            maxLines = 1
        )
    }
}