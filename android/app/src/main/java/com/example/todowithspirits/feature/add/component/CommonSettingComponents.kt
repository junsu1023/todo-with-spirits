package com.example.todowithspirits.feature.add.component

import androidx.compose.animation.AnimatedVisibility
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

@Composable
fun SettingSelectorItem(
    icon: Painter,
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    BaseSettingRow(
        icon = icon,
        label = label,
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
                        color = SpiritTodoTheme.color.todoTextMain
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

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 10.dp),
        thickness = 0.8.dp,
        color = SpiritTodoTheme.color.onSurfaceColor2
    )
}
