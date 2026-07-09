package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AlarmSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        modifier = Modifier.size(width = 48.dp, height = 24.dp),
        thumbContent = {
            Box(modifier = Modifier.size(20.dp))
        },
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = SpiritTodoTheme.colors.white,
            checkedTrackColor = SpiritTodoTheme.colors.onSurfaceColor1,
            checkedBorderColor = SpiritTodoTheme.colors.onSurfaceColor1,
            uncheckedThumbColor = SpiritTodoTheme.colors.white,
            uncheckedTrackColor = SpiritTodoTheme.colors.onSurfaceColor11,
            uncheckedBorderColor = SpiritTodoTheme.colors.onSurfaceColor11
        )
    )
}