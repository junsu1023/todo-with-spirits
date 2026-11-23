package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SettingActionRow(
    label: String,
    modifier: Modifier = Modifier,
    labelColor: Color = SpiritTodoTheme.color.font01,
    labelFontSize: TextUnit = 14.sp,
    labelFontWeight: FontWeight = FontWeight.Medium,
    trailingIconRes: Int = R.drawable.widgetly_arrow_16,
    onClick: (() -> Unit)? = null
) {
    SettingsRow(modifier = modifier, onClick = onClick) {
        Text(
            text = label,
            fontSize = labelFontSize,
            fontWeight = labelFontWeight,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(trailingIconRes),
            contentDescription = null
        )
    }
}
