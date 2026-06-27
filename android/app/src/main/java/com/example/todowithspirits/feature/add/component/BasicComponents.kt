package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * 프로젝트 공통 스위치 컴포넌트
 */
@Composable
fun SplitsTodoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.size(width = 52.dp, height = 26.dp),
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFFB286FD),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE0E0E0),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun SplitsTodoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedIcon: Painter,
    uncheckedIcon: Painter
) {
    Icon(
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        painter = if(checked) checkedIcon else uncheckedIcon,
        contentDescription = null,
        tint = Color.Unspecified
    )
}
