package com.example.todowithspirits.feature.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SelectionTabs
import com.example.todowithspirits.component.TabItems
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.add.component.SearchArea
import com.example.todowithspirits.feature.add.viewmodel.AddViewModel

@Composable
fun AddScreen(
    addViewModel: AddViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by addViewModel.uiState.collectAsState()

    val todoText = stringResource(R.string.todo)
    val routineText = stringResource(R.string.routine)
    val selectedTab = remember { mutableStateOf(todoText) }
    val tabItems = remember { TabItems(items = listOf(todoText, routineText)) }

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

            SelectionTabs(
                tabItems = tabItems,
                selectedItem = selectedTab.value,
                onItemSelected = { selectedTab.value = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedTab.value) {
                todoText -> TodoForm(
                    uiState = uiState,
                    onImportantChange = { addViewModel.setImportant(it) },
                    onDateChange = { addViewModel.setDate(it) },
                    onTimeEnabledChange = { addViewModel.setTimeEnabled(it) },
                    onDueTimeChange = { addViewModel.setDueTime(it) },
                    onAlarmOptionChange = { addViewModel.setAlarmOption(it) },
                    onCategoryOptionChange = { addViewModel.setCategoryOption(it) },
                    onPublicOptionChange = { addViewModel.setPublicOption(it) },
                    onMemoChange = { addViewModel.setMemo(it) }
                )
                else -> RoutineForm(
                    uiState = uiState,
                    onRepeatOptionChange = { addViewModel.setRepeatOption(it) },
                    onWeekDayToggled = { addViewModel.toggleWeekDay(it) },
                    onMonthDayToggled = { addViewModel.toggleMonthDay(it) },
                    onExcludeHolidaysChange = { addViewModel.setExcludeHolidays(it) },
                    onAlarmOptionChange = { addViewModel.setAlarmOption(it) },
                    onCategoryOptionChange = { addViewModel.setCategoryOption(it) },
                    onPublicOptionChange = { addViewModel.setPublicOption(it) },
                    onMemoChange = { addViewModel.setMemo(it) },
                    onRegisterClick = {
                        addViewModel.registerRoutine()
                        onBack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
