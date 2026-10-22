package com.example.todowithspirits.feature.add.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.domain.model.AlarmOption
import com.example.domain.model.RepeatOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TimeWheelPicker
import com.example.todowithspirits.feature.add.state.AddEvent
import com.example.todowithspirits.feature.add.state.AddUiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// 피그마: "23 : 59"
private val TimeValueFormatter = DateTimeFormatter.ofPattern("HH : mm")

// 중요 카드 + 날짜/시간/반복/알림 카드
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

    var isTimePickerOpen by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingGroup {
            SettingCheckboxItem(
                icon = painterResource(R.drawable.todo_important_20),
                label = stringResource(R.string.important),
                checked = uiState.isImportant,
                onCheckedChange = { onEvent(AddEvent.ImportantChanged(it)) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        SettingGroup {
            SettingDateItem(
                icon = painterResource(R.drawable.todo_calendar),
                label = stringResource(R.string.date),
                date = uiState.date,
                onDateSelected = { onEvent(AddEvent.DateChanged(it)) }
            )

            SettingDivider()

            SettingSwitchItem(
                icon = painterResource(R.drawable.todo_clock2),
                label = stringResource(R.string.time),
                checked = uiState.isTimeEnabled,
                onCheckedChange = { enabled ->
                    // 켜면 시간을 고를 수 있게 휠을 바로 펼친다.
                    isTimePickerOpen = enabled
                    onEvent(AddEvent.TimeEnabledChanged(enabled))
                },
                valueText = if (uiState.isTimeEnabled) uiState.dueTime.format(TimeValueFormatter) else null,
                isValueHighlighted = isTimePickerOpen,
                onValueClick = { isTimePickerOpen = !isTimePickerOpen },
                subContent = {
                    AnimatedVisibility(
                        visible = uiState.isTimeEnabled && isTimePickerOpen,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TimeWheelPicker(
                                initialHour = uiState.dueTime.hour,
                                initialMinute = uiState.dueTime.minute,
                                onTimeSelected = { h, m -> onEvent(AddEvent.DueTimeChanged(LocalTime.of(h, m))) }
                            )
                        }
                    }
                }
            )

            SettingDivider()

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
