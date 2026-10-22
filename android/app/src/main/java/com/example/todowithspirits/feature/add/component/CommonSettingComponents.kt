package com.example.todowithspirits.feature.add.component

import androidx.compose.animation.AnimatedVisibility
import java.time.DayOfWeek
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.SpiritsTodoDropdown
import com.example.todowithspirits.component.SpiritsTodoSwitch
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanDateWithDayFormatter
import java.time.LocalDate

private val SettingRowHorizontalPadding = 12.dp

@Composable
fun SettingGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SpiritTodoTheme.color.surfaceColor10,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(content = content)
    }
}

// 일정 추가 설정 한 줄: 아이콘 20 + 라벨 + 오른쪽 값/컨트롤. 높이 50 (세로 패딩 12 + 컨트롤 26)
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
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SettingRowHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = SpiritTodoTheme.color.todoTextMain
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

/**
 * @param valueText 스위치 왼쪽에 보여줄 값 (예: 켜진 시간). 누르면 [onValueClick].
 * @param isValueHighlighted 값을 강조(분홍)할지. 예: 시간 휠이 펼쳐진 상태
 */
@Composable
fun SettingSwitchItem(
    icon: Painter,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    valueText: String? = null,
    isValueHighlighted: Boolean = false,
    onValueClick: () -> Unit = {},
    subContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        subContent = subContent,
        action = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (valueText != null) {
                    Text(
                        text = valueText,
                        fontSize = 16.sp,
                        color = if (isValueHighlighted) SpiritTodoTheme.color.pointPink else SpiritTodoTheme.color.systemGrey,
                        modifier = Modifier.noRippleClickable(onClick = onValueClick)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                SpiritsTodoSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier
                        .width(44.dp)
                        .height(22.dp),
                    thumbSize = 18.dp,
                    checkedColor = SpiritTodoTheme.color.pointPink
                )
            }
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
            SpiritsTodoCheckbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                checkedIcon = painterResource(R.drawable.ic_checkbox_square_on),
                uncheckedIcon = painterResource(R.drawable.ic_checkbox_square_off),
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

/**
 * @param isValueHighlighted 값을 강조(분홍)할지. 예: 반복 매주/매월로 아래 선택 영역이 펼쳐진 상태
 */
@Composable
fun SettingSelectorItem(
    icon: Painter,
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isValueHighlighted: Boolean = false,
    subContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        subContent = subContent,
        action = {
            SpiritsTodoDropdown(
                value = value,
                options = options,
                onOptionSelected = onOptionSelected,
                selectedOptionColor = SpiritTodoTheme.color.pointPink
            ) { expand ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.noRippleClickable { expand() }
                ) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        color = if (isValueHighlighted) SpiritTodoTheme.color.pointPink else SpiritTodoTheme.color.todoTextMain
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Image(
                        painter = painterResource(R.drawable.expand_icon),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
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
                        selectedDate = date,
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
                text = date.format(KoreanDateWithDayFormatter),
                fontSize = 16.sp,
                color = if (expanded) SpiritTodoTheme.color.pointPink else SpiritTodoTheme.color.todoTextMain,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    )
}

// 반복 '매주'에서 요일 선택. 누른 채로 좌우로 끌면 지나간 요일을 한 번에 켜거나 끈다.
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
    val itemBounds = remember { arrayOfNulls<Rect>(weekDays.size) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                val currentSelected = BooleanArray(weekDays.size)
                var lastIndex = -1
                var lastDirection = 0
                var targetSelected = true

                fun indexAt(position: Offset): Int =
                    itemBounds.indexOfFirst { it?.contains(position) == true }

                fun applyTo(index: Int) {
                    if (currentSelected[index] != targetSelected) {
                        currentSelected[index] = targetSelected
                        currentOnDayToggled(weekDays[index].second)
                    }
                }

                fun beginDrag(index: Int) {
                    weekDays.forEachIndexed { i, (_, day) ->
                        currentSelected[i] = day in currentSelectedDays
                    }
                    targetSelected = !currentSelected[index]
                    lastIndex = index
                    lastDirection = 0
                    applyTo(index)
                }

                fun dragTo(index: Int) {
                    if (index == -1 || index == lastIndex) return
                    val direction = if (index > lastIndex) 1 else -1

                    if (lastDirection != 0 && direction != lastDirection) {
                        targetSelected = !targetSelected
                        applyTo(lastIndex)
                    }

                    val path = if (direction > 0) lastIndex + 1..index else lastIndex - 1 downTo index
                    for (i in path) applyTo(i)

                    lastIndex = index
                    lastDirection = direction
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    lastIndex = -1

                    val downIndex = indexAt(down.position)
                    if (downIndex != -1) beginDrag(downIndex)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            val index = indexAt(change.position)
                            if (lastIndex == -1) {
                                if (index != -1) beginDrag(index)
                            } else {
                                dragTo(index)
                            }
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
                        color = if(isSelected) SpiritTodoTheme.color.pointPink else SpiritTodoTheme.color.transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = SpiritTodoTheme.color.surfaceColor1,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if(isSelected) SpiritTodoTheme.color.pointPink else SpiritTodoTheme.color.onSurfaceColor8,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 10.dp),
        thickness = 0.8.dp,
        color = SpiritTodoTheme.color.onSurfaceColor2
    )
}
