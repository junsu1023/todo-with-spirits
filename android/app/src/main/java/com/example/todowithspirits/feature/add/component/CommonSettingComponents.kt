package com.example.todowithspirits.feature.add.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SplitsTodoCheckbox
import com.example.todowithspirits.component.SplitsTodoSwitch
import com.example.todowithspirits.theme.SplitsTodoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SplitsTodoTheme.colors.bgColor1,
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
                    color = SplitsTodoTheme.colors.mainTextColor,
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
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    BaseSettingRow(
        icon = icon,
        label = label,
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
                        color = SplitsTodoTheme.colors.mainTextColor
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Image(
                        painter = painterResource(R.drawable.expand_icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(96.dp)
                        .background(SplitsTodoTheme.colors.white, RoundedCornerShape(8.dp))

                ) {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = option,
                                        color = if (option == value) SplitsTodoTheme.colors.selectedTabColor else SplitsTodoTheme.colors.mainTextColor,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            contentPadding = PaddingValues(vertical = 12.dp)
                        )
                        if (index < options.size - 1) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = SplitsTodoTheme.colors.dividerColor
                            )
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
                color = if (expanded) SplitsTodoTheme.colors.selectedDateTextColor else SplitsTodoTheme.colors.mainTextColor,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    )
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.8.dp,
        color = Color(0xFFEEEEEE)
    )
}
