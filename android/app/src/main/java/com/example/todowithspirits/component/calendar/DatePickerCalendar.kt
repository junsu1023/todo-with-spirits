package com.example.todowithspirits.component.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DatePickerCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
) {
    val weekDays = remember(firstDayOfWeek) { weekDayLabels(firstDayOfWeek) }
    val textMeasurer = rememberTextMeasurer()
    val weekDayStyle = LocalTextStyle.current.merge(TextStyle(fontSize = 12.sp))
    // 날짜 열의 기준이 되는 요일 글자 폭. 요일 줄과 날짜 줄이 같은 값으로 열 위치를 계산한다.
    val weekDayWidths = remember(weekDays, weekDayStyle, textMeasurer) {
        weekDays.map { textMeasurer.measure(it, weekDayStyle).size.width }
    }

    CalendarPager(
        selectedDate = selectedDate,
        modifier = modifier,
        header = { month, onPrevious, onNext ->
            DatePickerHeader(
                month = month,
                onPrevious = onPrevious,
                onNext = onNext
            )

            Spacer(modifier = Modifier.height(14.dp))

            DatePickerWeekHeader(
                weekDays = weekDays,
                weekDayWidths = weekDayWidths,
                style = weekDayStyle
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    ) { month ->
        DatePickerMonthGrid(
            month = month,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            firstDayOfWeek = firstDayOfWeek,
            weekDayWidths = weekDayWidths
        )
    }
}

@Composable
private fun DatePickerHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.month, month.monthValue),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.font01
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painter = painterResource(R.drawable.widgetly_left_18),
                contentDescription = null,
                modifier = Modifier.noRippleClickable(onClick = onPrevious)
            )

            Image(
                painter = painterResource(R.drawable.widgetly_right_18),
                contentDescription = null,
                modifier = Modifier.noRippleClickable(onClick = onNext)
            )
        }
    }
}

@Composable
private fun DatePickerWeekHeader(
    weekDays: List<String>,
    weekDayWidths: List<Int>,
    style: TextStyle
) {
    WeekColumnsRow(weekDayWidths = weekDayWidths) {
        weekDays.forEach { day ->
            Text(
                text = day,
                textAlign = TextAlign.Center,
                style = style,
                color = SpiritTodoTheme.color.font02
            )
        }
    }
}

@Composable
private fun DatePickerMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    firstDayOfWeek: DayOfWeek,
    weekDayWidths: List<Int>
) {
    val calendarDays = remember(month, firstDayOfWeek) { calendarDaysOf(month, firstDayOfWeek) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        calendarDays.chunked(7).forEach { week ->
            WeekColumnsRow(weekDayWidths = weekDayWidths) {
                week.forEach { calendarDay ->
                    Box(
                        modifier = Modifier.noRippleClickable { onDateSelected(calendarDay.date) },
                        contentAlignment = Alignment.Center
                    ) {
                        DatePickerDay(
                            day = calendarDay.date.dayOfMonth,
                            isSelected = calendarDay.date == selectedDate,
                            isCurrentMonth = calendarDay.isCurrentMonth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerDay(
    day: Int,
    isSelected: Boolean,
    isCurrentMonth: Boolean
) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .requiredSize(18.dp)
                .background(SpiritTodoTheme.color.font01, CircleShape)
        )
    }

    Text(
        text = day.toString(),
        color = when {
            isSelected -> SpiritTodoTheme.color.white
            !isCurrentMonth -> SpiritTodoTheme.color.surfaceColor15
            else -> SpiritTodoTheme.color.font01
        },
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
    )
}

/**
 * 요일 줄과 날짜 줄이 함께 쓰는 7열 배치.
 * 요일 글자 폭([weekDayWidths])을 양끝 맞춤으로 늘어놓는다. 첫 요일은 월 표시와, 마지막 요일은 다음(>) 화살표와 끝을 맞춘다.
 * (화살표 아이콘은 18dp 박스 안쪽 약 6dp 지점에서 끝나므로 오른쪽 여백을 그만큼 더 둔다)
 * 각 칸은 해당 요일 글자의 중심에 맞춰 놓는다. 칸 너비는 요일 글자 폭 + 열 사이 간격이라 누르는 영역도 넓다.
 */
@Composable
private fun WeekColumnsRow(
    weekDayWidths: List<Int>,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .padding(start = 14.dp, end = 20.dp)
    ) { measurables, constraints ->
        val gap = ((constraints.maxWidth - weekDayWidths.sum()) / (weekDayWidths.size - 1).toFloat()).coerceAtLeast(0f)
        val slotWidths = weekDayWidths.map { (it + gap).roundToInt() }
        val placeables = measurables.mapIndexed { index, measurable ->
            measurable.measure(Constraints.fixed(slotWidths[index], constraints.maxHeight))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var labelStart = 0f
            placeables.forEachIndexed { index, placeable ->
                val center = labelStart + weekDayWidths[index] / 2f
                placeable.place(x = (center - placeable.width / 2f).roundToInt(), y = 0)
                labelStart += weekDayWidths[index] + gap
            }
        }
    }
}
