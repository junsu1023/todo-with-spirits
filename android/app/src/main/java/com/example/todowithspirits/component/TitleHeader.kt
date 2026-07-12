package com.example.todowithspirits.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun TitleHeader(
    @DrawableRes leftIconRes: Int? = null,
    title: String? = null,
    @DrawableRes rightIconRes: Int? = null,
    onLeftIconClick: (() -> Unit)? = null,
    onRightIconClick: (() -> Unit)? = null,
    isAlarm: Boolean = false,
    rightComposable: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(SpiritTodoTheme.color.transparent)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(leftIconRes != null) {
            Image(
                painter = painterResource(leftIconRes),
                contentDescription = null,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onLeftIconClick?.invoke() }
                )
            )
        }

        if(title != null) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor1,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        } else {
            Box(modifier = Modifier.weight(1f))
        }

        if(isAlarm) {
            AlarmIconSection(
                alarmIconRes = rightIconRes!!,
                onAlarmClick = onRightIconClick!!,
                msgCnt = 100
            )
        } else if(rightComposable != null) {
          rightComposable()
        } else if(rightIconRes != null) {
            Image(
                painter = painterResource(rightIconRes),
                contentDescription = null,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onRightIconClick?.invoke() }
                )
            )
        }
    }
}

@Composable
fun AlarmIconSection(
    alarmIconRes: Int,
    onAlarmClick: () -> Unit,
    msgCnt: Int
) {
    Box(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = { onAlarmClick() }
        )
    ) {
        Image(
            painter = painterResource(alarmIconRes),
            contentDescription = null
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = -5.dp)
                .widthIn(24.dp)
                .heightIn(13.dp)
                .background(SpiritTodoTheme.color.surfaceColor3, RoundedCornerShape(14.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if(msgCnt > 99) "99+" else msgCnt.toString(),
                color = SpiritTodoTheme.color.onSurfaceColor3,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}