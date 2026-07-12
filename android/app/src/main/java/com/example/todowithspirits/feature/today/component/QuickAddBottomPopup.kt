package com.example.todowithspirits.feature.today.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat.getString
import com.example.domain.model.RepeatOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.BottomBarHeight
import com.example.todowithspirits.component.SelectionTabs
import com.example.todowithspirits.component.SpiritsTodoSwitch
import com.example.todowithspirits.component.TabItems
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.feature.add.component.DayOfWeekSelector
import com.example.todowithspirits.component.MonthlyCalendarView
import com.example.todowithspirits.component.TimeWheelPicker
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val routineRepeatOptions = listOf(
    RepeatOption.DAILY.displayName,
    RepeatOption.WEEKLY.displayName,
    RepeatOption.MONTHLY.displayName
)

@Composable
fun QuickAddBottomPopup(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(getString(context, R.string.todo)) }
    var title by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var isScheduleSectionVisible by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var isDateExpanded by remember { mutableStateOf(false) }
    var isTimeEnabled by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var repeatOption by remember { mutableStateOf(RepeatOption.DAILY) }
    var selectedWeekDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var selectedMonthDays by remember { mutableStateOf(setOf<Int>()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN) }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = BottomBarHeight)
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SpiritTodoTheme.color.surfaceColor2),
                color = SpiritTodoTheme.color.surfaceColor1
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(top = 14.dp, bottom = 12.dp)
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionTabs(
                            tabItems = TabItems(listOf(stringResource(R.string.todo), stringResource(R.string.routine))),
                            selectedItem = selectedTab,
                            onItemSelected = { tab ->
                                selectedTab = tab
                                isDateExpanded = false
                                isTimeEnabled = false
                                isScheduleSectionVisible = false
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Image(
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { isImportant = !isImportant }
                                ),
                            painter = if(isImportant) painterResource(R.drawable.todo_important) else painterResource(R.drawable.todo_important2),
                            contentDescription = null,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            color = SpiritTodoTheme.color.onSurfaceColor1
                        ),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "제목 없음",
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                color = SpiritTodoTheme.color.onSurfaceColor2
                                            )
                                        )
                                    }

                                    innerTextField()
                                }

                                if (selectedTab == stringResource(R.string.todo)) {
                                    Image(
                                        painter = painterResource(R.drawable.todo_clock),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                isScheduleSectionVisible = !isScheduleSectionVisible

                                                if (!isScheduleSectionVisible) {
                                                    isDateExpanded = false
                                                    isTimeEnabled = false
                                                }
                                            },
                                        colorFilter = ColorFilter.tint(
                                            if(isScheduleSectionVisible) SpiritTodoTheme.color.surfaceColor3
                                            else SpiritTodoTheme.color.onSurfaceColor6
                                        )
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(color = SpiritTodoTheme.color.onSurfaceColor2, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedTab) {
                        stringResource(R.string.todo) -> {
                            AnimatedVisibility(
                                visible = isScheduleSectionVisible,
                                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                            ) {
                                Column {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(8.dp))
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                isDateExpanded = !isDateExpanded
                                                if (isDateExpanded) isTimeEnabled = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.date),
                                                modifier = Modifier.weight(1f),
                                                fontSize = 14.sp,
                                                color = SpiritTodoTheme.color.onSurfaceColor1
                                            )

                                            selectedDate?.let { date ->
                                                Text(
                                                    text = date.format(dateFormatter),
                                                    fontSize = 14.sp,
                                                    color = if(isDateExpanded) SpiritTodoTheme.color.onSurfaceColor4 else SpiritTodoTheme.color.onSurfaceColor1
                                                )
                                            }
                                        }

                                        AnimatedVisibility(
                                            visible = isDateExpanded,
                                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                                        ) {
                                            Column {
                                                CalendarView(
                                                    selectedDate = selectedDate ?: LocalDate.now(),
                                                    onDateSelected = {
                                                        selectedDate = it
                                                        isDateExpanded = false
                                                    },
                                                    showMonthNavigation = false
                                                )
                                        
                                                Spacer(Modifier.height(24.dp))
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(8.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.time),
                                                modifier = Modifier.weight(1f),
                                                fontSize = 14.sp,
                                                color = SpiritTodoTheme.color.onSurfaceColor1
                                            )

                                            SpiritsTodoSwitch(
                                                checked = isTimeEnabled,
                                                onCheckedChange = { enabled ->
                                                    isTimeEnabled = enabled
                                                    if (enabled) isDateExpanded = false
                                                },
                                                modifier = Modifier.width(48.dp).height(24.dp),
                                                thumbSize = 20.dp
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isTimeEnabled,
                                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                TimeWheelPicker(
                                                    initialHour = selectedTime.hour,
                                                    initialMinute = selectedTime.minute,
                                                    onTimeSelected = { h, m ->
                                                        selectedTime = LocalTime.of(h, m)
                                                    }
                                                )

                                                Spacer(Modifier.height(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        stringResource(R.string.routine) -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        when(repeatOption) {
                                            RepeatOption.WEEKLY -> 113.dp
                                            RepeatOption.MONTHLY -> 219.dp
                                            else -> 45.dp
                                        }
                                    )
                                    .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp)
                            ) {
                                QuickRepeatRow(
                                    value = repeatOption.displayName,
                                    onOptionSelected = { repeatOption = RepeatOption.fromDisplayName(it) }
                                )

                                AnimatedVisibility(
                                    visible = repeatOption == RepeatOption.WEEKLY,
                                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                                ) {
                                    DayOfWeekSelector(
                                        selectedDays = selectedWeekDays,
                                        onDayToggled = { day ->
                                            selectedWeekDays = selectedWeekDays.toMutableSet().apply {
                                                if (day in this) remove(day) else add(day)
                                            }
                                        }
                                    )
                                }

                                AnimatedVisibility(
                                    visible = repeatOption == RepeatOption.MONTHLY,
                                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                                ) {
                                    MonthlyCalendarView(
                                        selectedDays = selectedMonthDays,
                                        onDayToggled = { day ->
                                            selectedMonthDays = selectedMonthDays.toMutableSet().apply {
                                                if (day in this) remove(day) else add(day)
                                            }
                                        },
                                        compact = true
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fi_rr_plus),
                            contentDescription = null,
                            tint = SpiritTodoTheme.color.onSurfaceColor6,
                            modifier = Modifier
                                .rotate(45f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onDismiss() }
                        )

                        Image(
                            painter = painterResource(R.drawable.fi_rr_plus),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRepeatRow(
    value: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.repeat),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.onSurfaceColor1
        )

        Box {
            Row(
                modifier = Modifier.clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor1
                )

                Spacer(Modifier.width(6.dp))

                Image(
                    painter = painterResource(R.drawable.fi_rr_up_down),
                    contentDescription = null,
                    modifier = Modifier.width(8.dp).height(11.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(96.dp)
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(8.dp))
            ) {
                routineRepeatOptions.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = option,
                                    color = if(option == value) SpiritTodoTheme.color.onSurfaceColor4
                                            else SpiritTodoTheme.color.onSurfaceColor1,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )

                    if (index < routineRepeatOptions.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = SpiritTodoTheme.color.surfaceColor4
                        )
                    }
                }
            }
        }
    }
}
