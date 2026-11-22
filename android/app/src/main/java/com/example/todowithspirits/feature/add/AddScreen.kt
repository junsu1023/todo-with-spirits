package com.example.todowithspirits.feature.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.add.component.SettingCheckboxItem
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.TaskAlarmRow
import com.example.todowithspirits.feature.add.component.TaskDateRow
import com.example.todowithspirits.feature.add.component.TaskRepeatRow
import com.example.todowithspirits.feature.add.component.TaskTimeRow
import com.example.todowithspirits.feature.add.component.TaskTitleField
import com.example.todowithspirits.feature.add.state.AddEffect
import com.example.todowithspirits.feature.add.state.AddEvent
import com.example.todowithspirits.feature.add.state.AddUiState
import com.example.todowithspirits.feature.add.viewmodel.AddViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

private val AddHorizontalMargin = 18.dp

@Composable
fun AddRoute(
    taskId: Long?,
    navigateBack: () -> Unit,
    addViewModel: AddViewModel = hiltViewModel()
) {
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by addViewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        taskId?.let { addViewModel.loadTaskForEdit(it) }
    }

    LaunchedEffect(addViewModel) {
        addViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(addViewModel.effect) { effect ->
        when (effect) {
            AddEffect.NavigateBack -> navigateBack()
        }
    }

    AddScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = addViewModel::onEvent
    )
}

@Composable
fun AddScreen(
    uiState: AddUiState,
    isLoading: Boolean,
    onEvent: (AddEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            AddHeader(
                isEditMode = uiState.isEditMode,
                onBackClicked = { onEvent(AddEvent.BackClicked) }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                TaskTitleField(
                    title = uiState.title,
                    onTitleChange = { onEvent(AddEvent.TitleChanged(it)) }
                )

                Spacer(modifier = Modifier.height(26.dp))

                ImportantCheckBox(
                    isImportant = uiState.isImportant,
                    onCheckChanged = { onEvent(AddEvent.ImportantChanged(it)) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskOptionCard(
                    uiState = uiState,
                    onEvent = onEvent
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                onClick = { onEvent(AddEvent.ConfirmClicked) },
                enabled = uiState.canConfirm,
                containerColor = SpiritTodoTheme.color.pointPink,
                modifier = Modifier.padding(start = AddHorizontalMargin, end = AddHorizontalMargin, bottom = 24.dp)
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun AddHeader(
    isEditMode: Boolean,
    onBackClicked: () -> Unit
) {
    TopNaviBar(
        modifier = Modifier.padding(horizontal = 17.dp),
        title = stringResource(if (isEditMode) R.string.edit_todo else R.string.add_todo),
        leftComposable = {
            Image(
                painter = painterResource(R.drawable.todo_back1),
                contentDescription = null,
                modifier = Modifier
                    .size(26.dp)
                    .noRippleClickable { onBackClicked() }
            )
        }
    )
}

@Composable
private fun ImportantCheckBox(
    isImportant: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    SettingGroup {
        SettingCheckboxItem(
            icon = painterResource(R.drawable.widgetly_important_20),
            label = stringResource(R.string.important),
            checked = isImportant,
            onCheckedChange = { check -> onCheckChanged(check) }
        )
    }
}

@Composable
private fun TaskOptionCard(
    uiState: AddUiState,
    onEvent: (AddEvent) -> Unit
) {
    SettingGroup {
        TaskDateRow(
            date = uiState.date,
            onDateSelected = { date -> onEvent(AddEvent.DateChanged(date)) }
        )

        SettingDivider()

        TaskTimeRow(
            isEnabled = uiState.isTimeEnabled,
            dueTime = uiState.dueTime,
            onEnabledChange = { enabled -> onEvent(AddEvent.TimeEnabledChanged(enabled)) },
            onTimeSelected = { time -> onEvent(AddEvent.DueTimeChanged(time)) }
        )

        SettingDivider()

        TaskRepeatRow(
            repeatOption = uiState.repeatOption,
            selectedWeekDays = uiState.selectedWeekDays,
            selectedMonthDays = uiState.selectedMonthDays,
            onRepeatOptionSelected = { option -> onEvent(AddEvent.RepeatOptionChanged(option)) },
            onWeekDayToggled = { day -> onEvent(AddEvent.WeekDayToggled(day)) },
            onMonthDayToggled = { day -> onEvent(AddEvent.MonthDayToggled(day)) }
        )

        SettingDivider()

        TaskAlarmRow(
            alarmOption = uiState.alarmOption,
            onAlarmOptionSelected = { option -> onEvent(AddEvent.AlarmOptionChanged(option)) }
        )
    }
}
