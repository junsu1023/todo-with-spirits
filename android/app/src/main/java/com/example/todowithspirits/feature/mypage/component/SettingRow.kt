package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SettingsRow
import com.example.todowithspirits.feature.mypage.SettingItem
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SettingRow(item: SettingItem, onClick: () -> Unit = {}) {
    SettingsRow(
        modifier = Modifier.padding(vertical = 15.dp),
        onClick = onClick
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = null
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.titleRes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(item.descRes),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.systemGrey
            )
        }

        Image(
            painter = painterResource(R.drawable.fi_rr_angle_small_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.color.systemGrey),
            modifier = Modifier.size(22.dp)
        )
    }
}