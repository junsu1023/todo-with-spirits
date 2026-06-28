package com.example.todowithspirits.feature.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.todowithspirits.feature.add.component.CalendarView
import com.example.todowithspirits.feature.add.component.SettingCheckboxItem
import com.example.todowithspirits.feature.add.component.SettingDivider
import com.example.todowithspirits.feature.add.component.SettingGroup
import com.example.todowithspirits.feature.add.component.SettingSelectorItem
import com.example.todowithspirits.feature.add.component.SettingSwitchItem
import com.example.todowithspirits.feature.add.component.TimeWheelPicker
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScheduleForm() {
    // 차후 viewModel로 이전 필요
    val isImportant = remember { mutableStateOf(false) }
    val isAllDay = remember { mutableStateOf(true) }
    val startDate = remember { mutableStateOf(LocalDate.now()) }
    val endDate = remember { mutableStateOf(LocalDate.now()) }
    val startTime = remember { mutableStateOf(LocalTime.of(0, 0)) }
    val endTime = remember { mutableStateOf(LocalTime.of(23, 59)) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val isStartPickerVisible = remember { mutableStateOf(false) }
    val isStartTimePickerVisible = remember { mutableStateOf(false) }
    val isEndPickerVisible = remember { mutableStateOf(false) }
    val isEndTimePickerVisible = remember { mutableStateOf(false) }
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

        Spacer(modifier = Modifier.height(14.dp))

        SettingGroup {
            SettingSwitchItem(
                icon = painterResource(R.drawable.clock_icon),
                label = stringResource(R.string.all_day),
                checked = isAllDay.value,
                onCheckedChange = {
                    isAllDay.value = it
                    if (it) {
                        isStartPickerVisible.value = false
                        isStartTimePickerVisible.value = false
                        isEndPickerVisible.value = false
                        isEndTimePickerVisible.value = false
                    }
                },
                subContent = {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 22.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 50.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = startDate.value.format(dateFormatter),
                                fontSize = 14.sp,
                                color = if (isStartPickerVisible.value) SpiritTodoTheme.colors.selectedDateTextColor else SpiritTodoTheme.colors.mainTextColor,
                                modifier = Modifier.clickable {
                                    isStartPickerVisible.value = !isStartPickerVisible.value
                                    isStartTimePickerVisible.value = false
                                    isEndPickerVisible.value = false
                                    isEndTimePickerVisible.value = false
                                }
                            )

                            if (!isAllDay.value) {
                                Text(
                                    text = startTime.value.format(timeFormatter),
                                    fontSize = 14.sp,
                                    color = if (isStartTimePickerVisible.value) SpiritTodoTheme.colors.selectedDateTextColor else SpiritTodoTheme.colors.mainTextColor,
                                    modifier = Modifier.clickable {
                                        isStartTimePickerVisible.value =
                                            !isStartTimePickerVisible.value
                                        isStartPickerVisible.value = false
                                        isEndPickerVisible.value = false
                                        isEndTimePickerVisible.value = false
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isStartPickerVisible.value,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 22.dp)) {
                                CalendarView(
                                    selectedStartDate = startDate.value,
                                    selectedEndDate = startDate.value,
                                    onDateSelected = { startDate.value = it }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isStartTimePickerVisible.value,
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
                                    initialHour = startTime.value.hour,
                                    initialMinute = startTime.value.minute,
                                    onTimeSelected = { h, m ->
                                        startTime.value = LocalTime.of(h, m)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 50.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = endDate.value.format(dateFormatter),
                                fontSize = 14.sp,
                                color = if (isEndPickerVisible.value) SpiritTodoTheme.colors.selectedDateTextColor else SpiritTodoTheme.colors.mainTextColor,
                                modifier = Modifier.clickable {
                                    isEndPickerVisible.value = !isEndPickerVisible.value
                                    isEndTimePickerVisible.value = false
                                    isStartPickerVisible.value = false
                                    isStartTimePickerVisible.value = false
                                }
                            )

                            if (!isAllDay.value) {
                                Text(
                                    text = endTime.value.format(timeFormatter),
                                    fontSize = 14.sp,
                                    color = if (isEndTimePickerVisible.value) SpiritTodoTheme.colors.selectedDateTextColor else SpiritTodoTheme.colors.mainTextColor,
                                    modifier = Modifier.clickable {
                                        isEndTimePickerVisible.value = !isEndTimePickerVisible.value
                                        isEndPickerVisible.value = false
                                        isStartPickerVisible.value = false
                                        isStartTimePickerVisible.value = false
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isEndPickerVisible.value,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 22.dp)) {
                                CalendarView(
                                    selectedStartDate = endDate.value,
                                    selectedEndDate = endDate.value,
                                    onDateSelected = { endDate.value = it }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isEndTimePickerVisible.value,
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
                                    initialHour = endTime.value.hour,
                                    initialMinute = endTime.value.minute,
                                    onTimeSelected = { h, m ->
                                        endTime.value = LocalTime.of(h, m)
                                    }
                                )
                            }
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

        Spacer(modifier = Modifier.height(26.dp))

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

        SplitsTodoPrimaryButton(
            text = stringResource(R.string.register),
            onClick = { /* 등록 로직 */ }
        )
    }
}
