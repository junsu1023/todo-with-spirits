package com.example.todowithspirits.feature.today.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.today.state.RoutineItem
import com.example.todowithspirits.feature.today.state.TodoItem
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dummyWeeklyEvents: Map<Int, List<Int>> = mapOf(
    0 to listOf(0, 1),
    1 to listOf(0, 0, 1),
    2 to listOf(0),
    3 to listOf(1, 1),
    4 to listOf(1),
    5 to listOf(1),
    6 to listOf(0, 1)
)

@Composable
fun TodayPlanSection(
    todos: List<TodoItem>,
    routines: List<RoutineItem>
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (EEEE)", Locale.KOREAN) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTodo by remember { mutableStateOf<TodoItem?>(null) }
    var selectedRoutine by remember { mutableStateOf<RoutineItem?>(null) }
    var isWeekExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(26.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedDate.format(dateFormatter),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.onSurfaceColor5
                )

                Spacer(Modifier.width(2.dp))

                Image(
                    painter = painterResource(R.drawable.todo_arrow1),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isWeekExpanded = !isWeekExpanded }
                        .rotate(if(isWeekExpanded) 180f else 0f)
                )
            }

            Text(
                text = stringResource(R.string.see_all_plan),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.onSurfaceColor6
            )
        }

        AnimatedVisibility(
            visible = isWeekExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(12.dp))

                WeeklyCalendarStrip(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    todoColor = SpiritTodoTheme.color.surfaceColor8,
                    routineColor = SpiritTodoTheme.color.surfaceColor9
                )
            }
        }

        Spacer(Modifier.height(19.dp))

        Column(modifier = Modifier.padding(horizontal = 2.dp)) {
            if (todos.isNotEmpty()) {
                SectionHeader(title = stringResource(R.string.todo))

                Spacer(Modifier.height(18.dp))

                todos.forEachIndexed { index, item ->
                    TodayListItem(
                        title = item.title,
                        isDone = item.isDone,
                        isImportant = item.isImportant,
                        onClick = { selectedTodo = item }
                    )

                    if (index != todos.lastIndex) Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (todos.isNotEmpty() && routines.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))

                HorizontalDivider(
                    color = SpiritTodoTheme.color.surfaceColor7,
                    thickness = 1.dp
                )

                Spacer(Modifier.height(24.dp))
            }

            if (routines.isNotEmpty()) {
                SectionHeader(title = stringResource(R.string.routine))

                Spacer(Modifier.height(18.dp))

                routines.forEachIndexed { index, item ->
                    TodayListItem(
                        title = item.title,
                        isDone = item.isDone,
                        isTodo = false,
                        onClick = { selectedRoutine = item }
                    )

                    if (index != routines.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (todos.isNotEmpty() || routines.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
            }
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
            dueDate = routine.dueDate,
            dueTime = routine.dueTime,
            memo = routine.memo,
            onDismiss = { selectedRoutine = null },
            onDelete = { selectedRoutine = null },
            onPostpone = { selectedRoutine = null },
            onEdit = { selectedRoutine = null }
        )
    }
}

@Composable
private fun WeeklyCalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    todoColor: Color,
    routineColor: Color
) {
    val weekStart = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
    val dayLabels = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (0..6).forEach { offset ->
            val date = weekStart.plusDays(offset.toLong())
            val isSelected = date == selectedDate
            val eventTypes = dummyWeeklyEvents[offset] ?: emptyList()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = if(isSelected) SpiritTodoTheme.color.surfaceColor2 else SpiritTodoTheme.color.surfaceColor4,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDateSelected(date) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if(isSelected) SpiritTodoTheme.color.onSurfaceColor4 else SpiritTodoTheme.color.onSurfaceColor5
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = dayLabels[offset],
                    fontSize = 10.sp,
                    color = if(isSelected) SpiritTodoTheme.color.onSurfaceColor4 else SpiritTodoTheme.color.onSurfaceColor5
                )

                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    eventTypes.take(2).forEach { type ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if(type == 0) todoColor else routineColor,
                                    shape = CircleShape
                                )
                        )
                    }

                    if (eventTypes.isEmpty()) {
                        Spacer(Modifier.size(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = SpiritTodoTheme.color.onSurfaceColor5
    )
}

@Composable
private fun TodayListItem(
    title: String,
    isDone: Boolean,
    isTodo: Boolean = true,
    isImportant: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = when {
                isDone && isTodo -> painterResource(R.drawable.todo_icon_check_pp)
                isDone ->painterResource(R.drawable.todo_icon_check_gn)
                else -> painterResource(R.drawable.todo_icon_check_bl)
            },
            contentDescription = null
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.onSurfaceColor5
        )

        if (isImportant) {
            Spacer(Modifier.width(4.dp))

            Image(
                painter = painterResource(R.drawable.todo_important),
                contentDescription = null
            )
        }
    }
}
