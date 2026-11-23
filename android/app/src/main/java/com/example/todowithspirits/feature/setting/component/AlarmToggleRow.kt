package com.example.todowithspirits.feature.setting.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.component.SpiritsTodoSwitch
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
            color = SpiritTodoTheme.color.font01,
            modifier = Modifier.weight(1f)
        )

        SpiritsTodoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.width(44.dp).height(22.dp),
            thumbSize = 18.dp
        )
    }
}
