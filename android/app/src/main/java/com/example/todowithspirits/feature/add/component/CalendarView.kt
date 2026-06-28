package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedEndDate)) }
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

    val rangeColor = SpiritTodoTheme.colors.selectedDateBoxColor
    val selectedColor = SpiritTodoTheme.colors.selectedDateBoxColor

    Column(modifier = modifier.fillMaxWidth()) {
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
                color = SpiritTodoTheme.colors.mainTextColor
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
                    color = SpiritTodoTheme.colors.textColor1
                )
            }
        }

        val chunks = daysInMonth.chunked(7)
        chunks.forEachIndexed { weekIdx, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEachIndexed { dayIdx, date ->
                    val isStart = date != null && date == selectedStartDate
                    val isEnd = date != null && date == selectedEndDate
                    val isRangeActive = selectedStartDate.isBefore(selectedEndDate)
                    val isInRange = date != null && isRangeActive && 
                                   date.isAfter(selectedStartDate) && date.isBefore(selectedEndDate)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = date != null,
                                onClick = { date?.let { onDateSelected(it) } },
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            if (isStart && isRangeActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .fillMaxHeight()
                                        .align(Alignment.CenterEnd)
                                        .background(rangeColor)
                                )
                            } else if (isEnd && isRangeActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .fillMaxHeight()
                                        .align(Alignment.CenterStart)
                                        .background(rangeColor)
                                )
                            } else if (isInRange) {
                                val shape = when {
                                    dayIdx == 0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                    dayIdx == 6 -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                                    else -> RoundedCornerShape(0.dp)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(rangeColor, shape)
                                )
                            }

                            if (isStart || isEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(selectedColor, CircleShape)
                                )
                            }

                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isStart || isEnd) Color.White else SpiritTodoTheme.colors.mainTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isStart || isEnd) FontWeight.SemiBold else FontWeight.Normal
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
            
            if (weekIdx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
