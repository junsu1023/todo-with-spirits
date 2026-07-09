package com.example.todowithspirits.feature.alarm.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.component.SplitsTodoSwitch
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AlarmToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SpiritTodoTheme.colors.mainTextColor,
            modifier = Modifier.weight(1f)
        )

        SplitsTodoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
