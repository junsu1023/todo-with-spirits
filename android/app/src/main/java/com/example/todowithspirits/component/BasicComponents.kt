package com.example.todowithspirits.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SpiritTodoTheme

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
        thumbContent = {
            Box(modifier = Modifier.size(22.dp))
        },
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
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedIcon: Painter,
    uncheckedIcon: Painter
) {
    Icon(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        painter = if(checked) checkedIcon else uncheckedIcon,
        contentDescription = null,
        tint = Color.Unspecified
    )
}

@Composable
fun SplitsTodoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SpiritTodoTheme.color.surfaceColor3
        )
    ) {
        Text(
            text = text,
            color = SpiritTodoTheme.colors.white,
            fontSize = 16.sp
        )
    }
}
