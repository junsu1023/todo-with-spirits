package com.example.todowithspirits.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SpiritsTodoDropdown(
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    dropdownWidth: Dp = 96.dp,
    dropdownGap: Dp = 0.dp,
    itemVerticalPadding: Dp = 12.dp,
    unselectedOptionColor: Color = SpiritTodoTheme.color.todoTextMain,
    anchor: @Composable (expand: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val gapPx = with(LocalDensity.current) { dropdownGap.roundToPx() }

    Box(modifier = modifier) {
        anchor { expanded = true }

        if (expanded) {
            Popup(
                popupPositionProvider = remember(gapPx) { DropdownPositionProvider(gapPx) },
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier.width(dropdownWidth),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                    color = SpiritTodoTheme.color.surfaceColor1
                ) {
                    Column {
                        options.forEachIndexed { index, option ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable {
                                        onOptionSelected(option)
                                        expanded = false
                                    }
                                    .padding(vertical = itemVerticalPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    color = if (option == value) SpiritTodoTheme.color.mainTextAndStroke else unselectedOptionColor,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
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

private class DropdownPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = (anchorBounds.right - popupContentSize.width).coerceAtLeast(0)
        val y = if (windowSize.height - anchorBounds.bottom - gapPx >= popupContentSize.height) {
            anchorBounds.bottom + gapPx
        } else {
            (anchorBounds.top - popupContentSize.height - gapPx).coerceAtLeast(0)
        }
        return IntOffset(x, y)
    }
}
