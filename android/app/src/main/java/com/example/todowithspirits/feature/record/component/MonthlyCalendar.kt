package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.YearMonth
import kotlin.collections.chunked
import kotlin.collections.forEach

@Composable
fun MonthlyCalendar(yearMonth: YearMonth, today: LocalDate) {
    val allCells = remember(yearMonth) {
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDay = yearMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.value % 7
        buildList {
            repeat(startOffset) { add(null) }
            for (day in 1..daysInMonth) { add(day) }
            val rem = size % 7
            if (rem != 0) repeat(7 - rem) { add(null) }
        }
    }

    val headers = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor8
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        allCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val date = yearMonth.atDay(day)
                            val isToday = date == today
                            val isPast = date.isBefore(today)
                            val hasStamp = isPast && day % 5 == 0

                            CalendarDayCell(
                                day = day,
                                isToday = isToday,
                                isPast = isPast,
                                hasStamp = hasStamp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isPast: Boolean,
    hasStamp: Boolean
) {
    Column(
        modifier = Modifier.padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isPast) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(Modifier.size(5.dp).background(SpiritTodoTheme.color.keyTodo, CircleShape))
                Box(Modifier.size(5.dp).background(SpiritTodoTheme.color.keyRoutine, CircleShape))
            }
        } else {
            Spacer(Modifier.height(5.dp))
        }

        Spacer(Modifier.height(4.dp))

        // 임시 스탬프
        if (hasStamp) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SpiritTodoTheme.color.onSurfaceColor2, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.temp_spirit),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SpiritTodoTheme.color.surfaceColor1)
                    .then(
                        if (isToday) Modifier.border(1.dp, SpiritTodoTheme.color.surfaceColor2, RoundedCornerShape(8.dp))
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$day",
                    fontSize = 12.sp,
                    color = when {
                        isToday -> SpiritTodoTheme.color.onSurfaceColor2
                        else -> SpiritTodoTheme.color.todoTextMain
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}