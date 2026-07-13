package com.example.todowithspirits.feature.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TimeWheelPicker
import com.example.todowithspirits.feature.add.component.SettingCheckboxItem
import com.example.todowithspirits.feature.add.component.SettingDateItem
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.SettingSelectorItem
import com.example.todowithspirits.feature.add.component.SettingSwitchItem
import com.example.todowithspirits.feature.add.state.AddUiState
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TodoForm(
    uiState: AddUiState,
    onImportantChange: (Boolean) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeEnabledChange: (Boolean) -> Unit,
    onDueTimeChange: (LocalTime) -> Unit,
    onAlarmOptionChange: (AlarmOption) -> Unit,
    onCategoryOptionChange: (CategoryOption) -> Unit,
    onPublicOptionChange: (PublicStateOption) -> Unit,
    onMemoChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingGroup {
            SettingCheckboxItem(
                icon = painterResource(R.drawable.todo_important2),
                label = stringResource(R.string.important),
                checked = uiState.isImportant,
                onCheckedChange = onImportantChange
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SettingGroup {
            SettingDateItem(
                icon = painterResource(R.drawable.todo_calendar),
                label = stringResource(R.string.date),
                date = uiState.date,
                onDateSelected = onDateChange
            )

            SettingDivider()

            SettingSwitchItem(
                icon = painterResource(R.drawable.todo_clock2),
                label = stringResource(R.string.time),
                checked = uiState.isTimeEnabled,
                onCheckedChange = onTimeEnabledChange,
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
                                textSize = 28,
                                onTimeSelected = { h, m ->
                                    onDueTimeChange(LocalTime.of(h, m))
                                }
                            )
                        }
                    }
                }
            )

            SettingDivider()

            SettingSelectorItem(
                icon = painterResource(R.drawable.todo_alarm2),
                label = stringResource(R.string.alarm),
                value = uiState.alarmOption.displayName,
                options = AlarmOption.getAllDisplayNames(),
                onOptionSelected = { onAlarmOptionChange(AlarmOption.fromDisplayName(it)) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

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

        Spacer(modifier = Modifier.height(26.dp))

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

        Spacer(modifier = Modifier.height(28.dp))

        // Todo 생성 API가 아직 없어 등록 동작은 연동하지 않는다
        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.register),
            onClick = { }
        )
    }
}
