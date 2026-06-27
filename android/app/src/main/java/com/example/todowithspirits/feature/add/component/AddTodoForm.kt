package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SplitsTodoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddPlanForm() {
    // 차후 viewModel로 이전 필요
    val isImportant = remember { mutableStateOf(false) }
    val isAllDay = remember { mutableStateOf(true) }
    val startDate = remember { mutableStateOf(LocalDate.now()) }
    val endDate = remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN) }

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
            SettingSwitchItem(
                icon = painterResource(R.drawable.clock_icon),
                label = stringResource(R.string.all_day),
                checked = isAllDay.value,
                onCheckedChange = { isAllDay.value = it },
                subContent = {
                    if (isAllDay.value) {
                        Column(modifier = Modifier.padding(top = 12.dp, start = 36.dp)) {
                            Text(
                                text = startDate.value.format(dateFormatter),
                                fontSize = 14.sp,
                                color = SplitsTodoTheme.colors.mainTextColor
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = endDate.value.format(dateFormatter),
                                fontSize = 14.sp,
                                color = SplitsTodoTheme.colors.mainTextColor
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = startDate.value.format(dateFormatter),
                                    fontSize = 14.sp,
                                    color = SplitsTodoTheme.colors.selectedDateTextColor
                                )

                                Text(
                                    text = "23:59",
                                    fontSize = 14.sp,
                                    color = SplitsTodoTheme.colors.mainTextColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            CalendarView(
                                selectedStartDate = startDate.value,
                                selectedEndDate = endDate.value,
                                onDateSelected = {
                                    startDate.value = it
                                    endDate.value = it
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = endDate.value.format(dateFormatter),
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "23:59",
                                    fontSize = 18.sp,
                                    color = Color.DarkGray
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
                value = "안 함", // 차후 UI/UX 나온 후 작업
                onClick = {}
            )
            
            SettingDivider()
            
            SettingSelectorItem(
                icon = painterResource(R.drawable.alarm_icon),
                label = stringResource(R.string.alarm),
                value = "10분 전", // 차후 UI/UX 나온 후 작업
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingGroup {
            SettingSelectorItem(
                icon = painterResource(R.drawable.category_icon),
                label = stringResource(R.string.category),
                value = "인간관계/약속", // 차후 UI/UX 나온 후 작업
                onClick = {}
            )
            
            SettingDivider()
            
            SettingSelectorItem(
                icon = painterResource(R.drawable.private_icon),
                label = stringResource(R.string.public_state),
                value = "비공개", // 차후 UI/UX 나온 후 작업
                onClick = {}
            )
        }
    }
}
