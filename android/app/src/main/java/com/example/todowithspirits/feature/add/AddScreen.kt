package com.example.todowithspirits.feature.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.domain.model.TaskType
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SelectionTabs
import com.example.todowithspirits.component.TabItems
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.add.component.SearchArea
import com.example.todowithspirits.feature.add.viewmodel.AddViewModel

@Composable
fun AddScreen(
    addViewModel: AddViewModel = hiltViewModel(),
    taskId: Long? = null,
    onBack: () -> Unit
) {
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode = taskId != null

    LaunchedEffect(taskId) {
        taskId?.let { addViewModel.loadTaskForEdit(it) }
    }

    val todoText = stringResource(R.string.todo)
    val routineText = stringResource(R.string.routine)
    val selectedTab = remember { mutableStateOf(todoText) }
    val tabItems = remember { TabItems(items = listOf(todoText, routineText)) }

    LaunchedEffect(uiState.editingTaskType) {
        when (uiState.editingTaskType) {
            TaskType.ROUTINE -> selectedTab.value = routineText
            TaskType.SCHEDULE -> selectedTab.value = todoText
            null -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            title = stringResource(R.string.add_todo)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SearchArea(
                title = uiState.title,
                onTitleChange = { addViewModel.setTitle(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isEditMode) {
                SelectionTabs(
                    tabItems = tabItems,
                    selectedItem = selectedTab.value,
                    onItemSelected = { selectedTab.value = it }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            when (selectedTab.value) {
                todoText -> TodoForm(
                    uiState = uiState,
                    isEditMode = isEditMode,
                    onImportantChange = { addViewModel.setImportant(it) },
                    onDateChange = { addViewModel.setDate(it) },
                    onTimeEnabledChange = { addViewModel.setTimeEnabled(it) },
                    onDueTimeChange = { addViewModel.setDueTime(it) },
                    onAlarmOptionChange = { addViewModel.setAlarmOption(it) },
                    onCategoryOptionChange = { addViewModel.setCategoryOption(it) },
                    onPublicOptionChange = { addViewModel.setPublicOption(it) },
                    onMemoChange = { addViewModel.setMemo(it) },
                    onRegisterClick = {
                        if (isEditMode) {
                            addViewModel.updateTodo(onSuccess = onBack)
                        } else {
                            addViewModel.registerTodo(onSuccess = onBack)
                        }
                    }
                )
                else -> RoutineForm(
                    uiState = uiState,
                    isEditMode = isEditMode,
                    onRepeatOptionChange = { addViewModel.setRepeatOption(it) },
                    onWeekDayToggled = { addViewModel.toggleWeekDay(it) },
                    onMonthDayToggled = { addViewModel.toggleMonthDay(it) },
                    onExcludeHolidaysChange = { addViewModel.setExcludeHolidays(it) },
                    onAlarmOptionChange = { addViewModel.setAlarmOption(it) },
                    onCategoryOptionChange = { addViewModel.setCategoryOption(it) },
                    onPublicOptionChange = { addViewModel.setPublicOption(it) },
                    onMemoChange = { addViewModel.setMemo(it) },
                    onRegisterClick = {
                        if (isEditMode) {
                            addViewModel.updateRoutine(onSuccess = onBack)
                        } else {
                            addViewModel.registerRoutine(onSuccess = onBack)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
