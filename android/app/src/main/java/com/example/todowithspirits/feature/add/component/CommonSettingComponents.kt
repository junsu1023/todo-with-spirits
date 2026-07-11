package com.example.todowithspirits.feature.add.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntRect
import com.example.todowithspirits.R
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.component.SplitsTodoCheckbox
import com.example.todowithspirits.component.SplitsTodoSwitch
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SpiritTodoTheme.colors.bgColor1,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun BaseSettingRow(
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    subContent: (@Composable ColumnScope.() -> Unit)? = null,
    action: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = icon,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = SpiritTodoTheme.colors.mainTextColor,
                    fontWeight = FontWeight.Medium
                )
            )

            Box(
                modifier = Modifier.heightIn(min = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                action()
            }
        }

        subContent?.let { it() }
    }
}

@Composable
fun SettingSwitchItem(
    icon: Painter,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        subContent = subContent,
        action = {
            SplitsTodoSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingCheckboxItem(
    icon: Painter,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        action = {
            SplitsTodoCheckbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                checkedIcon = painterResource(R.drawable.checked_checkbox),
                uncheckedIcon = painterResource(R.drawable.unckecked_checkbox)
            )
        }
    )
}

@Composable
fun SettingSelectorItem(
    icon: Painter,
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    subContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    BaseSettingRow(
        icon = icon,
        label = label,
        subContent = subContent,
        action = {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { expanded = true }
                ) {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Image(
                        painter = painterResource(R.drawable.expand_icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (expanded) {
                    Popup(
                        popupPositionProvider = object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset {
                                val x = (anchorBounds.right - popupContentSize.width).coerceAtLeast(0)
                                val y = if (windowSize.height - anchorBounds.bottom >= popupContentSize.height) {
                                    anchorBounds.bottom
                                } else {
                                    (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
                                }
                                return IntOffset(x, y)
                            }
                        },
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Surface(
                            modifier = Modifier.width(96.dp),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 4.dp,
                            color = SpiritTodoTheme.colors.white
                        ) {
                            Column {
                                options.forEachIndexed { index, option ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onOptionSelected(option)
                                                expanded = false
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option,
                                            color = if (option == value) SpiritTodoTheme.colors.onSurfaceColor1 else SpiritTodoTheme.colors.mainTextColor,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    
                                    if (index < options.size - 1) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = SpiritTodoTheme.colors.dividerColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SettingDateItem(
    icon: Painter,
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN) }

    BaseSettingRow(
        icon = icon,
        label = label,
        subContent = {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    CalendarView(
                        selectedStartDate = date,
                        selectedEndDate = date,
                        onDateSelected = {
                            onDateSelected(it)
                            expanded = false
                        }
                    )
                }
            }
        },
        action = {
            Text(
                text = date.format(dateFormatter),
                fontSize = 16.sp,
                color = if (expanded) SpiritTodoTheme.colors.selectedDateTextColor else SpiritTodoTheme.colors.mainTextColor,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    )
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekDays = listOf(
        "일" to DayOfWeek.SUNDAY,
        "월" to DayOfWeek.MONDAY,
        "화" to DayOfWeek.TUESDAY,
        "수" to DayOfWeek.WEDNESDAY,
        "목" to DayOfWeek.THURSDAY,
        "금" to DayOfWeek.FRIDAY,
        "토" to DayOfWeek.SATURDAY
    )

    val currentSelectedDays by rememberUpdatedState(selectedDays)
    val currentOnDayToggled by rememberUpdatedState(onDayToggled)
    // 각 요일 Box의 부모(Row) 기준 좌표. 드래그 포인터가 어느 요일 위에 있는지 판별하는 데 사용.
    val itemBounds = remember { arrayOfNulls<Rect>(weekDays.size) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                // 드래그 도중 이미 처리한 요일을 다시 토글하지 않도록 방문 기록을 유지하고,
                // 드래그가 시작된 요일의 반대 상태를 목표값으로 삼아 지나가는 요일들을 같은 방향으로 맞춘다.
                val visitedIndices = mutableSetOf<Int>()
                var dragTargetSelected = true

                fun indexAt(position: Offset): Int =
                    itemBounds.indexOfFirst { it?.contains(position) == true }

                fun applyDrag(index: Int) {
                    if (index == -1 || !visitedIndices.add(index)) return
                    val day = weekDays[index].second
                    val isSelected = day in currentSelectedDays
                    if (isSelected != dragTargetSelected) {
                        currentOnDayToggled(day)
                    }
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    visitedIndices.clear()

                    val startIndex = indexAt(down.position)
                    if (startIndex != -1) {
                        dragTargetSelected = weekDays[startIndex].second !in currentSelectedDays
                        applyDrag(startIndex)
                    }

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            applyDrag(indexAt(change.position))
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekDays.forEachIndexed { index, (label, day) ->
            val isSelected = day in selectedDays

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .onGloballyPositioned { coordinates ->
                        itemBounds[index] = coordinates.boundsInParent()
                    }
                    .border(
                        width = if(isSelected) 1.dp else 0.dp,
                        color = if(isSelected) SpiritTodoTheme.color.surfaceColor2 else SpiritTodoTheme.color.transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = SpiritTodoTheme.colors.white,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if(isSelected) SpiritTodoTheme.color.onSurfaceColor4 else SpiritTodoTheme.color.onSurfaceColor8,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.8.dp,
        color = Color(0xFFEEEEEE)
    )
}
