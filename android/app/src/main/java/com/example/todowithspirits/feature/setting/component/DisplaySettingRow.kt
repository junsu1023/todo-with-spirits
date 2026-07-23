package com.example.todowithspirits.feature.setting.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SettingsRow
import com.example.todowithspirits.component.SpiritsTodoDropdown
import com.example.todowithspirits.component.SpiritsTodoSwitch
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun DisplayToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsRow {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.weight(1f)
        )

        SpiritsTodoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.width(48.dp).height(24.dp),
            thumbSize = 20.dp
        )
    }
}

@Composable
fun DisplaySelectorRow(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    SettingsRow {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.weight(1f)
        )

        SpiritsTodoDropdown(
            value = value,
            options = options,
            onOptionSelected = onOptionSelected,
            dropdownWidth = 96.dp,
            dropdownGap = 3.dp,
            itemVerticalPadding = 14.dp,
            unselectedOptionColor = SpiritTodoTheme.color.systemGrey
        ) { expand ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.noRippleClickable { expand() }
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )

                Image(
                    painter = painterResource(R.drawable.todo_arrow2_20),
                    contentDescription = null
                )
            }
        }
    }
}
