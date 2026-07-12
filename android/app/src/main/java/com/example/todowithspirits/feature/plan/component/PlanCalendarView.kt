package com.example.todowithspirits.feature.plan.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.plan.PlanType
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PlanCalendarData(
    val types: List<PlanType>,
    val label: String? = null
)

@Composable
fun PlanType.color(): Color = when (this) {
    PlanType.TODO -> SpiritTodoTheme.color.surfaceColor9
    PlanType.ROUTINE -> SpiritTodoTheme.colors.onSurfaceColor5
}

@Composable
fun PlanCalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventData: Map<Int, PlanCalendarData> = emptyMap()
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val daysInMonth = remember(currentMonth) {
        val days = mutableListOf<Int?>()
        val firstDay = currentMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7 // Sunday=0
        repeat(offset) { days.add(null) }
        for (d in 1..currentMonth.lengthOfMonth()) days.add(d)
        days
    }
    val today = remember { LocalDate.now() }
    val headerFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy. MM. dd (EEEE)", Locale.KOREAN)
    }
    val selectedTabColor = SpiritTodoTheme.colors.selectedTabColor
    val surfaceColor1 = SpiritTodoTheme.color.surfaceColor14

    Column(modifier = modifier.fillMaxWidth().background(SpiritTodoTheme.colors.surfaceColor4)) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedDate.format(headerFormatter),
                fontSize = 16.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Image(
                    painter = painterResource(R.drawable.left),
                    contentDescription = null,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { currentMonth = currentMonth.minusMonths(1) }
                )

                Image(
                    painter = painterResource(R.drawable.right),
                    contentDescription = null,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { currentMonth = currentMonth.plusMonths(1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor7,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val chunks = daysInMonth.chunked(7)
        chunks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {
                val weekCells = if (week.size < 7) {
                    week + List(7 - week.size) { null }
                } else {
                    week
                }

                weekCells.forEach { day ->
                    val isToday = day != null && currentMonth.atDay(day) == today
                    val calendarData = day?.let { eventData[it] }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                enabled = day != null,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                day?.let { onDateSelected(currentMonth.atDay(it)) }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .then(
                                    if (isToday) Modifier.background(selectedTabColor, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 10.sp,
                                    color = if (isToday) SpiritTodoTheme.colors.white else SpiritTodoTheme.colors.mainTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (calendarData != null && calendarData.types.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                calendarData.types.forEach { planType ->
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(planType.color(), CircleShape)
                                    )
                                }
                            }
                        }

                        if (calendarData?.label != null) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .background(surfaceColor1, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = calendarData.label,
                                    fontSize = 10.sp,
                                    color = SpiritTodoTheme.colors.mainTextColor,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
