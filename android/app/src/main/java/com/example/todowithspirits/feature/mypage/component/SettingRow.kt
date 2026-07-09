package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.mypage.SettingItem
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SettingRow(item: SettingItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.mainTextColor),
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.titleRes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(item.descRes),
                fontSize = 12.sp,
                color = SpiritTodoTheme.colors.onSurfaceColor2
            )
        }

        Image(
            painter = painterResource(R.drawable.fi_rr_angle_small_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.onSurfaceColor2),
            modifier = Modifier.size(22.dp)
        )
    }
}