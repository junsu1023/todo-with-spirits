package com.example.todowithspirits.feature.add.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.todowithspirits.util.TimeWheelDueTimeFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TaskDateRow(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    SettingDateItem(
        icon = painterResource(R.drawable.widgetly_calendar_20),
        label = stringResource(R.string.date),
        date = date,
        onDateSelected = onDateSelected
    )
}

@Composable
fun TaskTimeRow(
    isEnabled: Boolean,
    dueTime: LocalTime,
    onEnabledChange: (Boolean) -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    var isTimePickerOpen by rememberSaveable { mutableStateOf(false) }

    SettingSwitchItem(
        icon = painterResource(R.drawable.widgetly_clock_20),
        label = stringResource(R.string.time),
        checked = isEnabled,
        onCheckedChange = { enabled ->
            isTimePickerOpen = enabled
            onEnabledChange(enabled)
        },
        valueText = when(isEnabled) {
            true -> dueTime.format(TimeWheelDueTimeFormatter)
            false -> null
        },
        onValueClick = { isTimePickerOpen = !isTimePickerOpen },
        subContent = {
            AnimatedVisibility(
                visible = isEnabled && isTimePickerOpen,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimeWheelPicker(
                        initialHour = dueTime.hour,
                        initialMinute = dueTime.minute,
                        onTimeSelected = { h, m -> onTimeSelected(LocalTime.of(h, m)) }
                    )
                }
            }
        }
    )
}

@Composable
fun TaskRepeatRow(
    date: LocalDate,
    repeatOption: RepeatOption,
    selectedWeekDays: Set<DayOfWeek>,
    selectedMonthDays: Set<Int>,
    onRepeatOptionSelected: (RepeatOption) -> Unit,
    onWeekDayToggled: (DayOfWeek) -> Unit,
    onMonthDayToggled: (Int) -> Unit
) {
    SettingSelectorItem(
        icon = painterResource(R.drawable.widgetly_repeat_20),
        label = stringResource(R.string.repeat),
        value = repeatOption.displayName,
        options = RepeatOption.getAllDisplayNames(),
        onOptionSelected = { label -> onRepeatOptionSelected(RepeatOption.fromDisplayName(label)) },
        subContent = {
            when(repeatOption) {
                RepeatOption.WEEKLY -> {
                    DayOfWeekSelector(
                        selectedDays = selectedWeekDays,
                        onDayToggled = onWeekDayToggled,
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp)
                    )
                }
                RepeatOption.MONTHLY -> {
                    ExpandableDatePicker(
                        selectedDate = date,
                        onDateSelected = { selected -> onMonthDayToggled(selected.dayOfMonth) },
                        isDateSelected = { it.dayOfMonth in selectedMonthDays }
                    )
                }
                else -> Unit
            }
        }
    )
}

@Composable
fun TaskAlarmRow(
    alarmOption: AlarmOption,
    onAlarmOptionSelected: (AlarmOption) -> Unit
) {
    SettingSelectorItem(
        icon = painterResource(R.drawable.widgetly_alarm_20),
        label = stringResource(R.string.alarm),
        value = alarmOption.displayName,
        options = AlarmOption.entries.map { it.displayName },
        onOptionSelected = { label -> onAlarmOptionSelected(AlarmOption.fromDisplayName(label)) }
    )
}
