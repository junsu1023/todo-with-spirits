package com.example.todowithspirits.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 아래로 꼬리가 달린 말풍선.
 *
 * @param tailHorizontalBias 꼬리의 가로 위치(0f = 왼쪽 끝, 1f = 오른쪽 끝).
 * @param tailHeight 본문 아래로 내려오는 꼬리 높이. 컴포저블 전체 높이 = 본문 + 이 값.
 */
@Composable
fun SpeechBubble(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    horizontalPadding: Dp = 14.dp,
    verticalPadding: Dp = 6.dp,
    cornerRadius: Dp = 18.dp,
    tailWidth: Dp = 10.dp,
    tailHeight: Dp = 12.dp,
    tailHorizontalBias: Float = 0.6f
) {
    val shape = remember(cornerRadius, tailWidth, tailHeight, tailHorizontalBias) {
        SpeechBubbleShape(cornerRadius, tailWidth, tailHeight, tailHorizontalBias)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = verticalPadding,
                bottom = verticalPadding + tailHeight
            )
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}

private class SpeechBubbleShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp,
    private val tailHeight: Dp,
    private val tailHorizontalBias: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val tw = with(density) { tailWidth.toPx() }
        val th = with(density) { tailHeight.toPx() }
        val bodyBottom = (size.height - th).coerceAtLeast(0f)

        val minTipX = cr
        val maxTipX = (size.width - cr - tw).coerceAtLeast(minTipX)
        val tipX = minTipX + (maxTipX - minTipX) * tailHorizontalBias.coerceIn(0f, 1f)

        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = bodyBottom,
                    cornerRadius = CornerRadius(cr, cr)
                )
            )
            moveTo(tipX, bodyBottom)
            lineTo(tipX, size.height)
            lineTo(tipX + tw, bodyBottom)
            close()
        }

        return Outline.Generic(path)
    }
}
