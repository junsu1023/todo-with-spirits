package com.example.todowithspirits.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SpiritsTodoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    thumbSize: Dp
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (checked) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.surfaceColor15
            )
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val padding = (maxHeight - thumbSize) / 2
        val thumbOffset by animateDpAsState(
            targetValue = if (checked) maxWidth - thumbSize - padding else padding,
            label = "switchThumbOffset"
        )

        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(thumbSize)
                .background(SpiritTodoTheme.color.surfaceColor1, CircleShape)
        )
    }
}

@Composable
fun SpiritsTodoCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedIcon: Painter,
    uncheckedIcon: Painter
) {
    Icon(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onCheckedChange(!checked) }
            ),
        painter = if(checked) checkedIcon else uncheckedIcon,
        contentDescription = null,
        tint = Color.Unspecified
    )
}

@Composable
fun SpiritsTodoPrimaryButton(
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
            color = SpiritTodoTheme.color.surfaceColor1,
            fontSize = 16.sp
        )
    }
}
