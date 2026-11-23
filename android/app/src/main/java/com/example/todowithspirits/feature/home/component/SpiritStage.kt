package com.example.todowithspirits.feature.home.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

private val SpiritSize = 168.dp
private val StageHorizontalMargin = 18.dp
private val SpiritMenuIcons = listOf(
    SpiritMenuIcon(SpiritMenuType.ADD, R.drawable.widgetly_float_calendar, (-73).dp, (-70).dp),
    // TODO: 정령 기능 나오면 아이콘 widgetly_float_friendship 으로 변경
    SpiritMenuIcon(SpiritMenuType.FRIENDSHIP, R.drawable.widgetly_float_friendship_disabled, (-29).dp, (-110).dp, isComingSoon = true),
    SpiritMenuIcon(SpiritMenuType.CLOTH, R.drawable.widgetly_float_cloth, 29.dp, (-110).dp),
    SpiritMenuIcon(SpiritMenuType.SETTING, R.drawable.widgetly_float_setting, 73.dp, (-70).dp)
)
private enum class SpiritMenuType { ADD, FRIENDSHIP, CLOTH, SETTING }

private data class SpiritMenuIcon(
    val type: SpiritMenuType,
    @param:DrawableRes val iconRes: Int,
    val offsetX: Dp,
    val offsetY: Dp,
    val isComingSoon: Boolean = false
)

@Composable
fun SpiritStage(
    spiritName: String,
    message: String,
    isMenuVisible: Boolean,
    onSpiritClick: () -> Unit,
    onMenuAddClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val spiritBottomPadding = 212.dp

        val minX = with(density) { StageHorizontalMargin.toPx() }
        val maxX = with(density) { (maxWidth - StageHorizontalMargin - SpiritSize).toPx() }.coerceAtLeast(minX)
        val stageWidthPx = with(density) { maxWidth.toPx() }

        val spiritX = remember { Animatable((minX + maxX) / 2f) }
        var isMovingRight by remember { mutableStateOf(true) }

        // 아이콘 메뉴가 떠 있는 동안에는 이동을 멈추고, 닫히면 그 자리에서 다시 움직인다.
        LaunchedEffect(minX, maxX, isMenuVisible) {
            spiritX.snapTo(spiritX.value.coerceIn(minX, maxX))
            if (maxX <= minX || isMenuVisible) return@LaunchedEffect

            val pxPerMs = with(density) { 30f.dp.toPx() } / 1000f
            while (true) {
                val target = if (isMovingRight) maxX else minX
                val durationMs = (abs(target - spiritX.value) / pxPerMs).roundToInt()
                spiritX.animateTo(target, tween(durationMs, easing = LinearEasing))
                isMovingRight = !isMovingRight
            }
        }

        val bounceY = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        val bouncePx = with(density) { 20.dp.toPx() }
        val bubbleOffsetPx = with(density) { (-12).dp.toPx() }
        var bubbleWidthPx by remember { mutableIntStateOf(0) }
        val bubbleAlpha by animateFloatAsState(
            targetValue = if (isMenuVisible) 0f else 1f,
            label = "spiritBubbleAlpha"
        )

        // 0 = 정령 중심에 모여 있음, 1 = 각 위치로 펼쳐짐. 펼칠 때는 튕기듯, 접을 때는 빠르게 모인다.
        val menuProgress = remember { Animatable(0f) }
        LaunchedEffect(isMenuVisible) {
            if (isMenuVisible) {
                menuProgress.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            } else {
                menuProgress.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = spiritBottomPadding + SpiritSize - 20.dp)
                .graphicsLayer { alpha = bubbleAlpha }
                .onSizeChanged { bubbleWidthPx = it.width }
                .offset {
                    val maxBubbleX = (stageWidthPx - minX - bubbleWidthPx).coerceAtLeast(minX)
                    val x = (spiritX.value + bubbleOffsetPx).coerceIn(minX, maxBubbleX)
                    IntOffset(x.roundToInt(), 0)
                }
        ) {
            SpeechBubble(
                text = message,
                backgroundColor = SpiritTodoTheme.color.white,
                textColor = SpiritTodoTheme.color.font01,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                horizontalPadding = 12.dp,
                verticalPadding = 8.dp,
                cornerRadius = 6.dp,
                tailWidth = 7.dp,
                tailHeight = 24.dp,
                tailHorizontalBias = 0.47f,
                tailTipOffset = 10.dp,
                border = BorderStroke(1.dp, SpiritTodoTheme.color.stroke01)
            )

            Image(
                painter = painterResource(R.drawable.widgetly_heart_medium),
                contentDescription = null,
                modifier = Modifier.offset(x = (-1).dp, y = (-7).dp)
            )
            Image(
                painter = painterResource(R.drawable.widgetly_heart_large),
                contentDescription = null,
                modifier = Modifier.offset(x = 7.dp, y = (-14).dp)
            )
            Image(
                painter = painterResource(R.drawable.widgetly_heart_small),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 7.dp, y = 21.dp)
            )
        }

        // 가로 이동은 offset으로 적용해 클릭 영역이 정령을 따라오게 하고, 반전/바운스는 graphicsLayer로 처리한다.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = spiritBottomPadding)
                .offset { IntOffset(spiritX.value.roundToInt(), 0) }
                .size(SpiritSize)
                .graphicsLayer {
                    translationY = bounceY.value
                    scaleX = if(isMovingRight) -1f else 1f
                }
                .noRippleClickable {
                    onSpiritClick()
                    // 바운스 중 다시 누르면 진행 중인 애니메이션이 취소되고 현재 위치에서 다시 시작한다.
                    scope.launch {
                        bounceY.animateTo(-bouncePx, tween(150, easing = FastOutSlowInEasing))
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
                painter = painterResource(R.drawable.widgetly_wind_spirit),
                contentDescription = spiritName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            )
        }

        if (isMenuVisible || menuProgress.value > 0f) {
            val spiritHalfPx = with(density) { (SpiritSize / 2).toPx() }
            val iconHalfPx = with(density) { ( 44.dp / 2).toPx() }
            // 정령이 화면 끝에 있어도 아이콘이 좌우 여백 밖으로 나가지 않도록 아이콘 묶음 중심을 제한한다.
            val menuHalfWidthPx = with(density) { SpiritMenuIcons.maxOf { abs(it.offsetX.value) }.dp.toPx() } + iconHalfPx
            val minCenterX = minX + menuHalfWidthPx
            val maxCenterX = (stageWidthPx - minX - menuHalfWidthPx).coerceAtLeast(minCenterX)

            SpiritMenuIcons.forEach { icon ->
                val onClick: (() -> Unit)? = when (icon.type) {
                    SpiritMenuType.ADD -> onMenuAddClick
                    SpiritMenuType.SETTING -> onSettingClick
                    else -> null
                }

                // "출시 예정" 라벨이 아이콘과 같이 펼쳐지고 접히도록 같은 Box에 둔다.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // 아이콘 중심을 정령 박스 중심 높이에 맞춘 뒤, 진행도만큼 각 위치로 이동한다.
                        .padding(bottom = spiritBottomPadding + SpiritSize / 2 -  22.dp)
                        .offset {
                            val progress = menuProgress.value
                            val centerX = (spiritX.value + spiritHalfPx).coerceIn(minCenterX, maxCenterX)
                            IntOffset(
                                x = (centerX - iconHalfPx + icon.offsetX.toPx() * progress).roundToInt(),
                                y = (icon.offsetY.toPx() * progress).roundToInt()
                            )
                        }
                        .graphicsLayer {
                            val progress = menuProgress.value
                            val scale = 0.3f + (1f - 0.3f) * progress
                            scaleX = scale
                            scaleY = scale
                            alpha = progress.coerceIn(0f, 1f)
                            // 영역 밖의 "출시 예정" 라벨이 잘리지 않도록 임시 버퍼 없이 투명도를 적용한다.
                            compositingStrategy = CompositingStrategy.ModulateAlpha
                        }
                        // 닫히는 중에는 누르지 못하게 한다.
                        .then(
                            if (onClick != null && isMenuVisible) Modifier.noRippleClickable(onClick = onClick)
                            else Modifier
                        )
                ) {
                    Image(
                        painter = painterResource(icon.iconRes),
                        contentDescription = null
                    )

                    if (icon.isComingSoon) {
                        ComingSoonBadge(modifier = Modifier.matchParentSize())
                    }
                }
            }
        }
    }
}
