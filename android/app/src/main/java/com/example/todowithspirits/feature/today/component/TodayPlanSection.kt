package com.example.todowithspirits.feature.today.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// dummy data
private data class TodoItem(
    val title: String,
    val isDone: Boolean,
    val isImportant: Boolean,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val memo: String = ""
)
private data class RoutineItem(
    val title: String,
    val isDone: Boolean,
    val memo: String = ""
)

private val dummyTodos = listOf(
    TodoItem("성과 보고서 제출 마감", true, true, LocalDate.of(2026, 6, 1), LocalTime.of(19, 0)),
    TodoItem("26년도 하반기 KPI 목표 설정", true, true, LocalDate.of(2026, 7, 10)),
    TodoItem("월세 내기", false, false, LocalDate.now()),
    TodoItem("비행기 티켓 끊기", false, false)
)
private val dummyRoutines = listOf(
    RoutineItem("영어 단어 100개 외우기", true),
    RoutineItem("책 20 페이지 읽기", true)
)

@Composable
fun TodayPlanSection() {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (EEEE)", Locale.KOREAN) }
    val today = remember { LocalDate.now() }
    val todoCheckColor = SpiritTodoTheme.colors.onSurfaceColor4
    val routineCheckColor = SpiritTodoTheme.colors.onSurfaceColor5
    var selectedTodo by remember { mutableStateOf<TodoItem?>(null) }
    var selectedRoutine by remember { mutableStateOf<RoutineItem?>(null) }

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

            Spacer(Modifier.height(18.dp))

            dummyTodos.forEachIndexed { index,  item ->
                TodayListItem(
                    title = item.title,
                    isDone = item.isDone,
                    checkColor = todoCheckColor,
                    isImportant = item.isImportant,
                    onClick = { selectedTodo = item }
                )
                
                if(index != dummyTodos.lastIndex) Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(
                color = SpiritTodoTheme.colors.dividerColor,
                thickness = 0.8.dp
            )

            Spacer(Modifier.height(24.dp))

            SectionHeader(title = stringResource(R.string.routine))

            Spacer(Modifier.height(18.dp))

            dummyRoutines.forEachIndexed { index, item ->
                TodayListItem(
                    title = item.title,
                    isDone = item.isDone,
                    checkColor = routineCheckColor,
                    onClick = { selectedRoutine = item }
                )

                if(index != dummyRoutines.lastIndex) Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    selectedTodo?.let { todo ->
        TodoDetailBottomSheet(
            title = todo.title,
            isImportant = todo.isImportant,
            dueDate = todo.dueDate,
            dueTime = todo.dueTime,
            memo = todo.memo,
            onDismiss = { selectedTodo = null },
            onDelete = { selectedTodo = null },
            onPostpone = { selectedTodo = null },
            onEdit = { selectedTodo = null }
        )
    }

    selectedRoutine?.let { routine ->
        TodoDetailBottomSheet(
            title = routine.title,
            isImportant = false,
            dueDate = null,
            dueTime = null,
            memo = routine.memo,
            onDismiss = { selectedRoutine = null },
            onDelete = { selectedRoutine = null },
            onPostpone = { selectedRoutine = null },
            onEdit = { selectedRoutine = null }
        )
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
    isImportant: Boolean = false,
    onClick: () -> Unit = {}
) {
    val white = SpiritTodoTheme.colors.white

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            if (isDone) {
                drawCircle(color = checkColor)

                val path = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.50f)
                    lineTo(size.width * 0.43f, size.height * 0.68f)
                    lineTo(size.width * 0.78f, size.height * 0.33f)
                }

                drawPath(
                    path = path,
                    color = white,
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

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            color = SpiritTodoTheme.colors.mainTextColor
        )

        if (isImportant) {
            Spacer(Modifier.width(4.dp))

            Image(
                painter = painterResource(R.drawable.fi_rr_color_star),
                contentDescription = null
            )
        }
    }
}