package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SplitsTodoTheme

@Composable
fun SettingGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = SplitsTodoTheme.colors.bgColor1,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun BaseSettingRow(
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    subContent: (@Composable ColumnScope.() -> Unit)? = null,
    action: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = if (subContent == null) Alignment.CenterVertically else Alignment.Top
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = if (subContent == null) 0.dp else 2.dp)
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = SplitsTodoTheme.colors.textColor1,
                    fontWeight = FontWeight.Medium
                )
            )

            subContent?.let {
                Spacer(modifier = Modifier.height(4.dp))

                it()
            }
        }

        Box(
            modifier = Modifier.heightIn(min = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            action()
        }
    }
}

@Composable
fun SettingSwitchItem(
    icon: Painter,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        subContent = subContent,
        action = {
            SplitsTodoSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingCheckboxItem(
    icon: Painter,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        action = {
            SplitsTodoCheckbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                checkedIcon = painterResource(R.drawable.checked_checkbox),
                uncheckedIcon = painterResource(R.drawable.unckecked_checkbox)
            )
        }
    )
}

@Composable
fun SettingSelectorItem(
    icon: Painter,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    BaseSettingRow(
        icon = icon,
        label = label,
        onClick = onClick,
        action = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    painter = painterResource(R.drawable.expand_icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.LightGray
                )
            }
        }
    )
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.8.dp,
        color = Color(0xFFEEEEEE)
    )
}
