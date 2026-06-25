package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import com.example.todowithspirits.R

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
        val dayOfWeekOfFirstDay = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday
        
        repeat(dayOfWeekOfFirstDay) {
            days.add(null)
        }
        
        for (day in 1..currentMonth.lengthOfMonth()) {
            days.add(currentMonth.atDay(day))
        }
        days
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currentMonth.month.value}월",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8F8170)
            )
            Row {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Image(
                        painter = painterResource(R.drawable.left),
                        contentDescription = null
                    )
                }

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Image(
                        painter = painterResource(R.drawable.right),
                        contentDescription = null
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            val weekDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }

        // Grid for days
        val chunks = daysInMonth.chunked(7)
        chunks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isSelected = date != null && (date == selectedStartDate || date == selectedEndDate)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable(enabled = date != null) { date?.let { onDateSelected(it) } },
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFB286FD), CircleShape)
                                )
                            }
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                // Fill the rest of the week if necessary
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
