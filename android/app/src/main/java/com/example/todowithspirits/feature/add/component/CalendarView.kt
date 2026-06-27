package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.todowithspirits.theme.SplitsTodoTheme

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedStartDate)) }
    val daysInMonth = remember(currentMonth) {
        val days = mutableListOf<LocalDate?>()
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekOfFirstDay = firstDayOfMonth.dayOfWeek.value % 7
        
        repeat(dayOfWeekOfFirstDay) {
            days.add(null)
        }
        
        for (day in 1..currentMonth.lengthOfMonth()) {
            days.add(currentMonth.atDay(day))
        }
        days
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.month, currentMonth.monthValue),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SplitsTodoTheme.colors.mainTextColor
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Image(
                    painter = painterResource(R.drawable.left),
                    contentDescription = null,
                    modifier = Modifier.clickable(onClick = { currentMonth = currentMonth.minusMonths(1)})
                )

                Image(
                    painter = painterResource(R.drawable.right),
                    contentDescription = null,
                    modifier = Modifier.clickable(onClick = { currentMonth = currentMonth.plusMonths(1)})
                )
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
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SplitsTodoTheme.colors.textColor1
                )
            }
        }

        val chunks = daysInMonth.chunked(7)
        chunks.forEachIndexed { idx, week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isSelected = date != null && (date == selectedStartDate || date == selectedEndDate)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = date != null) {
                                date?.let { onDateSelected(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isSelected) SplitsTodoTheme.colors.selectedDateTextColor else SplitsTodoTheme.colors.mainTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

            if(idx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
