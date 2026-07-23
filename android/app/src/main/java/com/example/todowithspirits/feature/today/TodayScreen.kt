package com.example.todowithspirits.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.today.component.BadgeAndAchievementRow
import com.example.todowithspirits.feature.today.component.SpiritSection
import com.example.todowithspirits.feature.today.component.TodayPlanSection
import com.example.todowithspirits.feature.today.viewmodel.TodayViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun TodayScreen(
    todayViewModel: TodayViewModel = hiltViewModel(),
    navigateToAlarm: () -> Unit,
    navigateToEditTask: (Long) -> Unit
) {
    val uiState by todayViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor4)
            .verticalScroll(rememberScrollState())
    ) {
        TitleHeader(
            leftIconRes = R.drawable.temp_app_icon,
            rightIconRes = R.drawable.todo_alarm,
            onRightIconClick = navigateToAlarm,
            isAlarm = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SpiritSection(uiState.spiritInfo)

            BadgeAndAchievementRow(achievementRate = uiState.todayAchievementRate)

            Spacer(Modifier.height(18.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(SpiritTodoTheme.color.surfaceColor1)
                .padding(horizontal = 16.dp)
        ) {
            TodayPlanSection(
                selectedDate = uiState.selectedDate,
                onDateSelected = { todayViewModel.setSelectedDate(it) },
                todos = uiState.todos,
                routines = uiState.routines,
                weekEvents = uiState.weekEvents,
                onCompleteTask = { taskId, date -> todayViewModel.completeTask(taskId, date) },
                onCancelCompleteTask = { taskId, date -> todayViewModel.cancelTaskCompletion(taskId, date) },
                onDeleteTask = { taskId -> todayViewModel.deleteTask(taskId) },
                onEditTask = navigateToEditTask,
                onPostponeTodo = { taskId -> todayViewModel.postponeTodo(taskId) }
            )
        }
    }
}
