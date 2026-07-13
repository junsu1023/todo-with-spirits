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
import com.example.todowithspirits.component.MonthlyCalendarView
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.feature.add.component.DayOfWeekSelector
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.SettingSelectorItem
import com.example.todowithspirits.feature.add.state.AddUiState
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.DayOfWeek

private val routineRepeatOptions = listOf(
    RepeatOption.DAILY.displayName,
    RepeatOption.WEEKLY.displayName,
    RepeatOption.MONTHLY.displayName
)

@Composable
fun RoutineForm(
    uiState: AddUiState,
    onRepeatOptionChange: (RepeatOption) -> Unit,
    onWeekDayToggled: (DayOfWeek) -> Unit,
    onMonthDayToggled: (Int) -> Unit,
    onExcludeHolidaysChange: (Boolean) -> Unit,
    onAlarmOptionChange: (AlarmOption) -> Unit,
    onCategoryOptionChange: (CategoryOption) -> Unit,
    onPublicOptionChange: (PublicStateOption) -> Unit,
    onMemoChange: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_repeat),
                label = stringResource(R.string.repeat),
                value = uiState.repeatOption.displayName,
                options = routineRepeatOptions,
                onOptionSelected = { onRepeatOptionChange(RepeatOption.fromDisplayName(it)) },
                subContent = {
                    when (uiState.repeatOption) {
                        RepeatOption.WEEKLY -> {
                            Spacer(modifier = Modifier.height(22.dp))

                            DayOfWeekSelector(
                                selectedDays = uiState.selectedWeekDays,
                                onDayToggled = onWeekDayToggled,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                        RepeatOption.MONTHLY -> {
                            Spacer(modifier = Modifier.height(22.dp))

                            MonthlyCalendarView(
                                selectedDays = uiState.selectedMonthDays,
                                onDayToggled = onMonthDayToggled,
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
            checked = uiState.excludeHolidays,
            onCheckedChange = onExcludeHolidaysChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_alarm2),
                label = stringResource(R.string.alarm),
                value = uiState.alarmOption.displayName,
                options = AlarmOption.getAllDisplayNames(),
                onOptionSelected = { onAlarmOptionChange(AlarmOption.fromDisplayName(it)) }
            )
        }

        Spacer(modifier = Modifier.height(13.dp))

        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_category),
                label = stringResource(R.string.category),
                value = uiState.categoryOption.displayName,
                options = CategoryOption.getAllDisplayNames(),
                onOptionSelected = { onCategoryOptionChange(CategoryOption.fromDisplayName(it)) }
            )

            SettingDivider()

            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_private),
                label = stringResource(R.string.public_state),
                value = uiState.publicOption.displayName,
                options = PublicStateOption.getAllDisplayNames(),
                onOptionSelected = { onPublicOptionChange(PublicStateOption.fromDisplayName(it)) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        BasicTextField(
            value = uiState.memo,
            onValueChange = onMemoChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, SpiritTodoTheme.color.onSurfaceColor2, RoundedCornerShape(6.dp))
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = SpiritTodoTheme.color.todoTextMain
            ),
            decorationBox = { innerTextField ->
                if (uiState.memo.isEmpty()) {
                    Text(
                        text = "메모",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = SpiritTodoTheme.color.onSurfaceColor2
                        )
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(38.dp))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.register),
            onClick = onRegisterClick
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
                color = SpiritTodoTheme.color.onSurfaceColor8
            )
        )

        SpiritsTodoCheckbox(
            modifier = Modifier.size(18.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            checkedIcon = painterResource(R.drawable.todo_check_pp),
            uncheckedIcon = painterResource(R.drawable.todo_check_up)
        )
    }
}
