package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun RecordHeader(navigateToAlarm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.record),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.colors.mainTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { navigateToAlarm() }
        ) {
            Image(
                painter = painterResource(R.drawable.alarm_icon),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(SpiritTodoTheme.colors.buttonColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "99+",
                    color = SpiritTodoTheme.colors.white,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}