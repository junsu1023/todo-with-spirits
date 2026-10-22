package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.YearMonth

private val DayRowHeight = 32.dp
private val SelectedCircleSize = 18.dp

// 반복 '매월'에서 날짜(일) 여러 개 선택. 달을 넘겨도 선택은 '며칠'(1~31) 기준으로 유지된다.
@Composable
fun MonthlyCalendarView(
    selectedDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = remember(currentMonth) {
        val days = mutableListOf<Int?>()
        repeat(currentMonth.atDay(1).dayOfWeek.value % 7) { days.add(null) }
        for (day in 1..currentMonth.lengthOfMonth()) days.add(day)
        days
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.month, currentMonth.monthValue),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Image(
                    painter = painterResource(R.drawable.left),
                    contentDescription = null,
                    modifier = Modifier.noRippleClickable { currentMonth = currentMonth.minusMonths(1) }
                )

                Image(
                    painter = painterResource(R.drawable.right),
                    contentDescription = null,
                    modifier = Modifier.noRippleClickable { currentMonth = currentMonth.plusMonths(1) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor9
                )
            }
        }

        daysInMonth.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DayRowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { day ->
                    val isSelected = day != null && day in selectedDays

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .noRippleClickable(enabled = day != null) { day?.let(onDayToggled) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(SelectedCircleSize)
                                        .background(SpiritTodoTheme.color.pointPink, CircleShape)
                                )
                            }

                            Text(
                                text = day.toString(),
                                color = if (isSelected) SpiritTodoTheme.color.onSurfaceColor3 else SpiritTodoTheme.color.todoTextMain,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
