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
import com.example.todowithspirits.component.SplitsTodoPrimaryButton
import com.example.todowithspirits.feature.add.component.SettingCheckboxItem
import com.example.todowithspirits.feature.add.component.SettingDateItem
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.SettingSelectorItem
import com.example.todowithspirits.feature.add.component.SettingSwitchItem
import com.example.todowithspirits.feature.add.component.TimeWheelPicker
import com.example.todowithspirits.theme.SplitsTodoTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TodoForm() {
    val isImportant = remember { mutableStateOf(false) }
    val dueDate = remember { mutableStateOf(LocalDate.now()) }
    val isTimeEnabled = remember { mutableStateOf(false) }
    val dueTime = remember { mutableStateOf(LocalTime.of(0, 0)) }
    val repeatOption = remember { mutableStateOf(RepeatOption.NONE) }
    val alarmOption = remember { mutableStateOf(AlarmOption.TEN_MIN_BEFORE) }
    val categoryOption = remember { mutableStateOf(CategoryOption.RELATIONSHIP) }
    val publicOption = remember { mutableStateOf(PublicStateOption.PRIVATE) }
    
    val memoValue = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingGroup {
            SettingCheckboxItem(
                icon = painterResource(R.drawable.important_icon),
                label = stringResource(R.string.important),
                checked = isImportant.value,
                onCheckedChange = { isImportant.value = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingGroup {
            SettingDateItem(
                icon = painterResource(R.drawable.clock_icon),
                label = stringResource(R.string.due_date),
                date = dueDate.value,
                onDateSelected = { dueDate.value = it }
            )

            SettingDivider()

            SettingSwitchItem(
                icon = painterResource(R.drawable.clock_icon),
                label = stringResource(R.string.time),
                checked = isTimeEnabled.value,
                onCheckedChange = { isTimeEnabled.value = it },
                subContent = {
                    AnimatedVisibility(
                        visible = isTimeEnabled.value,
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
                                initialHour = dueTime.value.hour,
                                initialMinute = dueTime.value.minute,
                                onTimeSelected = { h, m ->
                                    dueTime.value = LocalTime.of(h, m)
                                }
                            )
                        }
                    }
                }
            )

            SettingDivider()

            SettingSelectorItem(
                icon = painterResource(R.drawable.repeat_icon),
                label = stringResource(R.string.repeat),
                value = repeatOption.value.displayName,
                options = RepeatOption.getAllDisplayNames(),
                onOptionSelected = { repeatOption.value = RepeatOption.fromDisplayName(it) }
            )

            SettingDivider()

            SettingSelectorItem(
                icon = painterResource(R.drawable.alarm_icon),
                label = stringResource(R.string.alarm),
                value = alarmOption.value.displayName,
                options = AlarmOption.getAllDisplayNames(),
                onOptionSelected = { alarmOption.value = AlarmOption.fromDisplayName(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(26.dp))

        BasicTextField(
            value = memoValue.value,
            onValueChange = { memoValue.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, SplitsTodoTheme.colors.dividerColor, RoundedCornerShape(6.dp))
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = SplitsTodoTheme.colors.mainTextColor
            ),
            decorationBox = { innerTextField ->
                if (memoValue.value.isEmpty()) {
                    Text(
                        text = "메모",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = SplitsTodoTheme.colors.hintTextColor
                        )
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(38.dp))

        SplitsTodoPrimaryButton(
            text = stringResource(R.string.register),
            onClick = { }
        )
    }
}
