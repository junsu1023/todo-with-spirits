package com.example.todowithspirits.feature.alarm.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AlarmHeader(
    onBack: () -> Unit,
    onSettingClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 16.dp, top = 18.dp, bottom = 10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_back),
            contentDescription = null,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onBack() },
            colorFilter = ColorFilter.tint(SpiritTodoTheme.color.onSurfaceColor1)
        )

        Text(
            text = stringResource(R.string.alarm),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.colors.mainTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(R.drawable.fi_rr_setting),
            contentDescription = null,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSettingClick() }
        )
    }
}