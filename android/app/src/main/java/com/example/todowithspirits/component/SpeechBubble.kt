package com.example.todowithspirits.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
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
 * @param tailTipOffset 꼬리 끝이 꼬리 밑변 왼쪽 점에서 가로로 떨어진 거리. 0이면 왼쪽 변이 수직인 직각 꼬리.
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
    tailHorizontalBias: Float = 0.6f,
    tailTipOffset: Dp = 0.dp,
    border: BorderStroke? = null
) {
    val shape = remember(cornerRadius, tailWidth, tailHeight, tailHorizontalBias, tailTipOffset) {
        SpeechBubbleShape(cornerRadius, tailWidth, tailHeight, tailHorizontalBias, tailTipOffset)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
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
    private val tailHorizontalBias: Float,
    private val tailTipOffset: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val tw = with(density) { tailWidth.toPx() }
        val th = with(density) { tailHeight.toPx() }
        val tipOffset = with(density) { tailTipOffset.toPx() }
        val bodyBottom = (size.height - th).coerceAtLeast(0f)

        val minTipX = cr
        val maxTipX = (size.width - cr - tw).coerceAtLeast(minTipX)
        val tipX = minTipX + (maxTipX - minTipX) * tailHorizontalBias.coerceIn(0f, 1f)

        // 본문과 꼬리를 하나의 윤곽으로 이어 그려야 테두리가 꼬리 밑변을 가로지르지 않는다.
        val right = size.width
        val path = Path().apply {
            moveTo(cr, 0f)
            lineTo(right - cr, 0f)
            arcTo(Rect(right - 2 * cr, 0f, right, 2 * cr), 270f, 90f, false)
            lineTo(right, bodyBottom - cr)
            arcTo(Rect(right - 2 * cr, bodyBottom - 2 * cr, right, bodyBottom), 0f, 90f, false)
            lineTo(tipX + tw, bodyBottom)
            lineTo(tipX + tipOffset, size.height)
            lineTo(tipX, bodyBottom)
            lineTo(cr, bodyBottom)
            arcTo(Rect(0f, bodyBottom - 2 * cr, 2 * cr, bodyBottom), 90f, 90f, false)
            lineTo(0f, cr)
            arcTo(Rect(0f, 0f, 2 * cr, 2 * cr), 180f, 90f, false)
            close()
        }

        return Outline.Generic(path)
    }
}
