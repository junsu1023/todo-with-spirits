package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.domain.model.AlarmOption
import com.example.domain.model.RepeatOption
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.add.state.AddEvent
import com.example.todowithspirits.feature.add.state.AddUiState

@Composable
fun TodoForm(
    uiState: AddUiState,
    onEvent: (AddEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val repeatOptions = RepeatOption.entries
    val repeatLabels = repeatOptions.map { it.displayName }
    val alarmOptions = AlarmOption.entries
    val alarmLabels = alarmOptions.map { it.displayName }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_repeat),
                label = stringResource(R.string.repeat),
                value = uiState.repeatOption.displayName,
                options = repeatLabels,
                onOptionSelected = { label ->
                    repeatOptions.getOrNull(repeatLabels.indexOf(label))?.let { onEvent(AddEvent.RepeatOptionChanged(it)) }
                },
                isValueHighlighted = uiState.repeatOption == RepeatOption.WEEKLY || uiState.repeatOption == RepeatOption.MONTHLY,
                subContent = {
                    when (uiState.repeatOption) {
                        RepeatOption.WEEKLY -> DayOfWeekSelector(
                            selectedDays = uiState.selectedWeekDays,
                            onDayToggled = { onEvent(AddEvent.WeekDayToggled(it)) },
                            modifier = Modifier.padding(start = 10.dp, top = 12.dp, end = 10.dp)
                        )
                        RepeatOption.MONTHLY -> MonthlyCalendarView(
                            selectedDays = uiState.selectedMonthDays,
                            onDayToggled = { onEvent(AddEvent.MonthDayToggled(it)) },
                            modifier = Modifier.padding(start = 12.dp, top = 16.dp, end = 12.dp)
                        )
                        else -> Unit
                    }
                }
            )

            SettingDivider()

            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_alarm2),
                label = stringResource(R.string.alarm),
                value = uiState.alarmOption.displayName,
                options = alarmLabels,
                onOptionSelected = { label ->
                    alarmOptions.getOrNull(alarmLabels.indexOf(label))?.let { onEvent(AddEvent.AlarmOptionChanged(it)) }
                }
            )
        }
    }
}
