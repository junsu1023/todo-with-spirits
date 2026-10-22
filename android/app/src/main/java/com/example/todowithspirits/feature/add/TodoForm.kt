package com.example.todowithspirits.feature.add

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.domain.model.AlarmOption
import com.example.domain.model.RepeatOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TimeWheelPicker
import com.example.todowithspirits.feature.add.component.SettingCheckboxItem
import com.example.todowithspirits.feature.add.component.SettingDateItem
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.SettingSelectorItem
import com.example.todowithspirits.feature.add.component.SettingSwitchItem
import com.example.todowithspirits.feature.add.state.AddEvent
import com.example.todowithspirits.feature.add.state.AddUiState
import java.time.LocalTime

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
                onCheckedChange = { onEvent(AddEvent.TimeEnabledChanged(it)) },
                subContent = {
                    AnimatedVisibility(
                        visible = uiState.isTimeEnabled,
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
