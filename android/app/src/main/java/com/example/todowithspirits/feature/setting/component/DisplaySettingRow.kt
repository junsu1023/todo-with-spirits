package com.example.todowithspirits.feature.setting.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoSwitch
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun DisplayToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.weight(1f)
        )

        SpiritsTodoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.width(48.dp).height(24.dp),
            thumbSize = 20.dp
        )
    }
}

@Composable
fun DisplaySelectorRow(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.weight(1f)
        )

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { expanded = true }
                )
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )

                Image(
                    painter = painterResource(R.drawable.todo_arrow2_20),
                    contentDescription = null
                )
            }

            if (expanded) {
                Popup(
                    popupPositionProvider = object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize
                        ): IntOffset {
                            val x = (anchorBounds.right - popupContentSize.width).coerceAtLeast(0)
                            val y = if (windowSize.height - anchorBounds.bottom >= popupContentSize.height) {
                                anchorBounds.bottom + 8
                            } else {
                                (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
                            }
                            return IntOffset(x, y)
                        }
                    },
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        modifier = Modifier.width(140.dp),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 6.dp,
                        color = SpiritTodoTheme.color.surfaceColor1
                    ) {
                        Column {
                            options.forEachIndexed { index, option ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                onOptionSelected(option)
                                                expanded = false
                                            }
                                        )
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        color = if (option == value) SpiritTodoTheme.color.onSurfaceColor4 else SpiritTodoTheme.color.systemGrey,
                                        fontSize = 14.sp
                                    )
                                }

                                if (index < options.size - 1) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = SpiritTodoTheme.color.surfaceColor4
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
