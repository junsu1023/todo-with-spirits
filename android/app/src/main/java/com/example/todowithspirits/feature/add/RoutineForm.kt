package com.example.todowithspirits.feature.add

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AlarmOption
import com.example.domain.model.CategoryOption
import com.example.domain.model.PublicStateOption
import com.example.domain.model.RepeatOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.feature.add.component.DayOfWeekSelector
import com.example.todowithspirits.component.MonthlyCalendarView
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.SettingSelectorItem
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.DayOfWeek

private val routineRepeatOptions = listOf(
    RepeatOption.DAILY.displayName,
    RepeatOption.WEEKLY.displayName,
    RepeatOption.MONTHLY.displayName
)

@Composable
fun RoutineForm() {
    val repeatOption = remember { mutableStateOf(RepeatOption.DAILY) }
    val selectedWeekDays = remember { mutableStateOf(setOf<DayOfWeek>()) }
    val selectedMonthDays = remember { mutableStateOf(setOf<Int>()) }
    val excludeHolidays = remember { mutableStateOf(false) }
    val alarmOption = remember { mutableStateOf(AlarmOption.NONE) }
    val categoryOption = remember { mutableStateOf(CategoryOption.NONE) }
    val publicOption = remember { mutableStateOf(PublicStateOption.PRIVATE) }
    val memoValue = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.repeat_icon),
                label = stringResource(R.string.repeat),
                value = repeatOption.value.displayName,
                options = routineRepeatOptions,
                onOptionSelected = { repeatOption.value = RepeatOption.fromDisplayName(it) },
                subContent = {
                    when (repeatOption.value) {
                        RepeatOption.WEEKLY -> {
                            Spacer(modifier = Modifier.height(22.dp))

                            DayOfWeekSelector(
                                selectedDays = selectedWeekDays.value,
                                onDayToggled = { day ->
                                    selectedWeekDays.value = selectedWeekDays.value.toMutableSet().apply {
                                        if (day in this) remove(day) else add(day)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                        RepeatOption.MONTHLY -> {
                            Spacer(modifier = Modifier.height(22.dp))

                            MonthlyCalendarView(
                                selectedDays = selectedMonthDays.value,
                                onDayToggled = { day ->
                                    selectedMonthDays.value = selectedMonthDays.value.toMutableSet().apply {
                                        if (day in this) remove(day) else add(day)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                        else -> { /* Nothing */ }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        HolidayExcludeRow(
            checked = excludeHolidays.value,
            onCheckedChange = { excludeHolidays.value = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.alarm_icon),
                label = stringResource(R.string.alarm),
                value = alarmOption.value.displayName,
                options = AlarmOption.getAllDisplayNames(),
                onOptionSelected = { alarmOption.value = AlarmOption.fromDisplayName(it) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.category_icon),
                label = stringResource(R.string.category),
                value = categoryOption.value.displayName,
                options = CategoryOption.getAllDisplayNames(),
                onOptionSelected = { categoryOption.value = CategoryOption.fromDisplayName(it) }
            )

            SettingDivider()

            SettingSelectorItem(
                icon = painterResource(R.drawable.private_icon),
                label = stringResource(R.string.public_state),
                value = publicOption.value.displayName,
                options = PublicStateOption.getAllDisplayNames(),
                onOptionSelected = { publicOption.value = PublicStateOption.fromDisplayName(it) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        BasicTextField(
            value = memoValue.value,
            onValueChange = { memoValue.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, SpiritTodoTheme.colors.dividerColor, RoundedCornerShape(6.dp))
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            ),
            decorationBox = { innerTextField ->
                if (memoValue.value.isEmpty()) {
                    Text(
                        text = "메모",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = SpiritTodoTheme.colors.hintTextColor
                        )
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.register),
            onClick = { }
        )
    }
}

@Composable
private fun HolidayExcludeRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.exclude_holidays),
            style = TextStyle(
                fontSize = 14.sp,
                color = SpiritTodoTheme.colors.onSurfaceColor7
            )
        )

        SpiritsTodoCheckbox(
            modifier = Modifier.size(18.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            checkedIcon = painterResource(R.drawable.checked_checkbox),
            uncheckedIcon = painterResource(R.drawable.unckecked_checkbox)
        )
    }
}
