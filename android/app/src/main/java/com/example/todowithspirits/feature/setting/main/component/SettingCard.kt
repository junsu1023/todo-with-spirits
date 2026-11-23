package com.example.todowithspirits.feature.setting.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SettingActionRow
import com.example.todowithspirits.feature.setting.main.state.SettingMenu
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.font01
        )

        SettingCard(content = content)
    }
}

@Composable
fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(6.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.white, shape)
            .border(1.dp, SpiritTodoTheme.color.stroke01, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}


@Composable
fun SettingMenuRows(
    menus: List<SettingMenu>,
    onMenuClick: (SettingMenu) -> Unit,
    isSubdued: Boolean = false
) {
    menus.forEachIndexed { index, menu ->
        if (index > 0) SettingDivider()

        SettingActionRow(
            label = stringResource(menu.labelRes),
            modifier = Modifier.height(22.dp),
            labelColor = if (isSubdued) SpiritTodoTheme.color.fontGrey else SpiritTodoTheme.color.font02,
            labelFontSize = 14.sp,
            labelFontWeight = FontWeight.Normal,
            trailingIconRes = if (isSubdued) R.drawable.widgetly_arrow_grey_16 else R.drawable.widgetly_arrow_16,
            trailingIconTint = null,
            trailingIconSize = 16.dp,
            onClick = { onMenuClick(menu) }
        )
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(thickness = 0.5.dp, color = SpiritTodoTheme.color.stroke01)
}
