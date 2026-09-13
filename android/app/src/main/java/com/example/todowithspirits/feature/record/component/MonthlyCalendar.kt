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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthlyDailyHeatmap
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.YearMonth
import kotlin.collections.chunked
import kotlin.collections.forEach

@Composable
fun MonthlyCalendar(
    yearMonth: YearMonth,
    today: LocalDate,
    dailyHeatmaps: List<MonthlyDailyHeatmap> = emptyList()
) {
    val heatmapByDate = remember(dailyHeatmaps) { dailyHeatmaps.associateBy { it.date } }
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
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val date = yearMonth.atDay(day)
                            val isToday = date == today
                            val heatmap = heatmapByDate[date]
                            val scheduleTotal = heatmap?.scheduleTotalCount ?: 0
                            val scheduleCompleted = heatmap?.scheduleCompletedCount ?: 0
                            val routineTotal = heatmap?.routineTotalCount ?: 0
                            val routineCompleted = heatmap?.routineCompletedCount ?: 0
                            val hasSchedule = scheduleTotal > 0
                            val hasRoutine = routineTotal > 0
                            val totalCount = scheduleTotal + routineTotal
                            val completedCount = scheduleCompleted + routineCompleted
                            // 해당 일자의 모든 todo/루틴을 완료했을 때만 스탬프 표시
                            val hasStamp = totalCount > 0 && completedCount == totalCount

                            CalendarDayCell(
                                day = day,
                                isToday = isToday,
                                hasStamp = hasStamp,
                                hasSchedule = hasSchedule,
                                hasRoutine = hasRoutine
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
    hasStamp: Boolean,
    hasSchedule: Boolean,
    hasRoutine: Boolean
) {
    Column(
        modifier = Modifier.padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasSchedule || hasRoutine) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (hasSchedule) Box(Modifier.size(5.dp).background(SpiritTodoTheme.color.keyTodo, CircleShape))
                if (hasRoutine) Box(Modifier.size(5.dp).background(SpiritTodoTheme.color.keyRoutine, CircleShape))
            }
        } else {
            Spacer(Modifier.height(5.dp))
        }

        Spacer(Modifier.height(4.dp))

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
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(8.dp))
                    .then(
                        if (isToday) Modifier.border(1.dp, SpiritTodoTheme.color.mainTextAndStroke, RoundedCornerShape(8.dp))
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$day",
                    fontSize = 12.sp,
                    color = when {
                        isToday -> SpiritTodoTheme.color.mainTextAndStroke
                        else -> SpiritTodoTheme.color.todoTextMain
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}