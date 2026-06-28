package com.example.todowithspirits.feature.today.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// dummy data
private data class ScheduleItem(val title: String, val isDone: Boolean)
private data class TodoItem(val title: String, val isDone: Boolean, val isImportant: Boolean)
private data class RoutineItem(val title: String, val isDone: Boolean)

private val dummySchedules = listOf(
    ScheduleItem("민지랑 저녁", true)
)
private val dummyTodos = listOf(
    TodoItem("성과 보고서 제출 마감", true, true),
    TodoItem("26년도 하반기 KPI 목표 설정", true, true),
    TodoItem("월세 내기", false, false),
    TodoItem("비행기 티켓 끊기", false, false)
)
private val dummyRoutines = listOf(
    RoutineItem("영어 단어 100개 외우기", true),
    RoutineItem("책 20 페이지 읽기", true)
)

private val todoCheckColor = Color(0xFF5BBFDE)
private val routineCheckColor = Color(0xFF7DCB7F)

@Composable
fun TodayPlanSection() {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (EEEE)", Locale.KOREAN) }
    val today = remember { LocalDate.now() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = today.format(dateFormatter),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.colors.mainTextColor
                )

                Spacer(Modifier.width(2.dp))

                Image(
                    painter = painterResource(R.drawable.fi_rr_angle_small_down),
                    contentDescription = null
                )
            }

            Text(
                text = stringResource(R.string.see_all_plan),
                fontSize = 13.sp,
                color = SpiritTodoTheme.colors.onSurfaceColor2
            )
        }

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 2.dp)) {
            SectionHeader(title = stringResource(R.string.todo))

            Spacer(Modifier.height(8.dp))

            dummySchedules.forEach { item ->
                TodayListItem(
                    title = item.title,
                    isDone = item.isDone,
                    checkColor = SpiritTodoTheme.colors.selectedTabColor
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(
                color = SpiritTodoTheme.colors.dividerColor,
                thickness = 0.8.dp
            )

            Spacer(Modifier.height(16.dp))

            // 할 일
            SectionHeader("할 일")
            Spacer(Modifier.height(8.dp))
            dummyTodos.forEach { item ->
                TodayListItem(
                    title = item.title,
                    isDone = item.isDone,
                    checkColor = todoCheckColor,
                    isImportant = item.isImportant
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(
                color = SpiritTodoTheme.colors.dividerColor,
                thickness = 0.8.dp
            )

            Spacer(Modifier.height(16.dp))

            // 루틴
            SectionHeader("루틴")

            Spacer(Modifier.height(8.dp))
            dummyRoutines.forEach { item ->
                TodayListItem(
                    title = item.title,
                    isDone = item.isDone,
                    checkColor = routineCheckColor
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = SpiritTodoTheme.colors.mainTextColor
    )
}

@Composable
private fun TodayListItem(
    title: String,
    isDone: Boolean,
    checkColor: Color,
    isImportant: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            if (isDone) {
                drawCircle(color = checkColor)
                val path = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.50f)
                    lineTo(size.width * 0.43f, size.height * 0.68f)
                    lineTo(size.width * 0.78f, size.height * 0.33f)
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 2.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            } else {
                drawCircle(
                    color = Color(0xFFD5D5D5),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            color = SpiritTodoTheme.colors.mainTextColor,
            modifier = Modifier.weight(1f)
        )

        if (isImportant) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "★",
                color = SpiritTodoTheme.colors.selectedTabColor,
                fontSize = 14.sp
            )
        }
    }
}