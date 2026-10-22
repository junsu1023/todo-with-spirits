package com.example.todowithspirits.feature.home.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpeechBubble
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SPIRIT_SPEED_DP_PER_SEC = 30f
private const val BOUNCE_UP_DURATION_MS = 150

// wind_spirit 원본은 꼬리가 오른쪽에 있어 왼쪽을 바라본다. 오른쪽으로 갈 때 좌우 반전한다.
private const val SPIRIT_FACES_LEFT = true

private const val HILL_ASPECT_RATIO = 296.287f / 360f

private val SpiritSize = 150.dp
private val StageHorizontalMargin = 18.dp
private val BounceHeight = 20.dp

// home1 기준: 정령 하단이 언덕 상단에서 50 아래, 말풍선은 정령보다 50 왼쪽에서 시작해 정령 상단과 6 겹친다.
private val SpiritSinkIntoHill = 50.dp
private val BubbleOffsetX = (-50).dp
private val BubbleOverlapSpirit = 6.dp

@Composable
fun SpiritStage(
    spiritName: String,
    message: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val hillHeight = maxWidth * HILL_ASPECT_RATIO
        // 홈은 하단 버튼 뒤까지 그려지므로 언덕을 영역(=화면) 맨 아래에 맞춘다.
        val spiritBottomPadding = hillHeight - SpiritSinkIntoHill

        val minX = with(density) { StageHorizontalMargin.toPx() }
        val maxX = with(density) { (maxWidth - StageHorizontalMargin - SpiritSize).toPx() }.coerceAtLeast(minX)
        val stageWidthPx = with(density) { maxWidth.toPx() }

        val spiritX = remember { Animatable((minX + maxX) / 2f) }
        var isMovingRight by remember { mutableStateOf(true) }

        LaunchedEffect(minX, maxX) {
            spiritX.snapTo(spiritX.value.coerceIn(minX, maxX))
            if (maxX <= minX) return@LaunchedEffect

            val pxPerMs = with(density) { SPIRIT_SPEED_DP_PER_SEC.dp.toPx() } / 1000f
            while (true) {
                val target = if (isMovingRight) maxX else minX
                val durationMs = (abs(target - spiritX.value) / pxPerMs).roundToInt()
                spiritX.animateTo(target, tween(durationMs, easing = LinearEasing))
                isMovingRight = !isMovingRight
            }
        }

        val bounceY = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        val bouncePx = with(density) { BounceHeight.toPx() }
        val bubbleOffsetPx = with(density) { BubbleOffsetX.toPx() }
        var bubbleWidthPx by remember { mutableIntStateOf(0) }

        Image(
            painter = painterResource(R.drawable.home_hill),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(hillHeight)
        )

        // 꼬리가 정령을 가리키도록 정령을 따라 움직이되, 좌우 여백 밖으로는 나가지 않는다.
        SpeechBubble(
            text = message,
            backgroundColor = SpiritTodoTheme.color.systemArea,
            textColor = SpiritTodoTheme.color.todoTextMain,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            horizontalPadding = 26.dp,
            verticalPadding = 12.dp,
            cornerRadius = 21.dp,
            tailWidth = 13.dp,
            tailHeight = 31.dp,
            tailHorizontalBias = 0.28f,
            tailTipOffset = 19.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = spiritBottomPadding + SpiritSize - BubbleOverlapSpirit)
                .onSizeChanged { bubbleWidthPx = it.width }
                .offset {
                    val maxBubbleX = (stageWidthPx - minX - bubbleWidthPx).coerceAtLeast(minX)
                    val x = (spiritX.value + bubbleOffsetPx).coerceIn(minX, maxBubbleX)
                    IntOffset(x.roundToInt(), 0)
                }
        )

        // 가로 이동은 offset으로 적용해 클릭 영역이 정령을 따라오게 하고, 반전/바운스는 graphicsLayer로 처리한다.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = spiritBottomPadding)
                .offset { IntOffset(spiritX.value.roundToInt(), 0) }
                .size(SpiritSize)
                .graphicsLayer {
                    translationY = bounceY.value
                    scaleX = if (isMovingRight == SPIRIT_FACES_LEFT) -1f else 1f
                }
                .noRippleClickable {
                    // 바운스 중 다시 누르면 진행 중인 애니메이션이 취소되고 현재 위치에서 다시 시작한다.
                    scope.launch {
                        bounceY.animateTo(-bouncePx, tween(BOUNCE_UP_DURATION_MS, easing = FastOutSlowInEasing))
                        bounceY.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }
        ) {
            Image(
                painter = painterResource(R.drawable.wind_spirit),
                contentDescription = spiritName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
