package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SettingActionRow(
    label: String,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    description: String? = null,
    labelColor: Color = SpiritTodoTheme.color.todoTextMain,
    trailingIconRes: Int = R.drawable.fi_rr_angle_small_right,
    trailingIconTint: Color? = SpiritTodoTheme.color.systemGrey,
    trailingIconSize: Dp = 22.dp,
    onClick: (() -> Unit)? = null
) {
    SettingsRow(modifier = modifier, onClick = onClick) {
        if (iconRes != null) {
            Image(painter = painterResource(iconRes), contentDescription = null)

            Spacer(Modifier.width(14.dp))
        }

        if (description != null) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = labelColor
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )
            }
        } else {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor,
                modifier = Modifier.weight(1f)
            )
        }

        Image(
            painter = painterResource(trailingIconRes),
            contentDescription = null,
            colorFilter = trailingIconTint?.let { ColorFilter.tint(it) },
            modifier = Modifier.size(trailingIconSize)
        )
    }
}
