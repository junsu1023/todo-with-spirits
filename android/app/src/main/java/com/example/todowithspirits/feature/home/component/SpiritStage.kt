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

private const val SPIRIT_SPEED_DP_PER_SEC = 30f
private const val BOUNCE_UP_DURATION_MS = 150

// wind_spirit 원본은 꼬리가 오른쪽에 있어 왼쪽을 바라본다. 오른쪽으로 갈 때 좌우 반전한다.
private const val SPIRIT_FACES_LEFT = true

private val SpiritSize = 168.dp
private val StageHorizontalMargin = 18.dp
private val BounceHeight = 20.dp

// home1 기준: 정령 하단이 하단 버튼 상단에서 93 위(= 화면 하단 여백 24 + 버튼 94 + 93),
// 말풍선은 정령보다 13 왼쪽에서 시작해 꼬리 끝이 정령 상단과 21 겹친다.
private val SpiritBottomPadding = 212.dp
private val BubbleOffsetX = (-12).dp
private val BubbleOverlapSpirit = 20.dp

// home4 기준: 정령 박스 중심에서 각 아이콘 중심까지의 거리. 좌우 대칭으로 부채꼴처럼 놓인다.
private val MenuIconSize = 44.dp
private val SpiritMenuIcons = listOf(
    SpiritMenuIcon(SpiritMenuType.ADD, R.drawable.ic_spirit_menu_add, (-73).dp, (-70).dp),
    SpiritMenuIcon(SpiritMenuType.FRIENDSHIP, R.drawable.ic_spirit_menu_friendship, (-29).dp, (-110).dp),
    SpiritMenuIcon(SpiritMenuType.SPIRIT, R.drawable.ic_spirit_menu_spirit, 29.dp, (-110).dp),
    SpiritMenuIcon(SpiritMenuType.EMPTY, R.drawable.ic_spirit_menu_empty, 73.dp, (-70).dp)
)
private const val MENU_ICON_START_SCALE = 0.3f
private const val MENU_CLOSE_DURATION_MS = 200

// Note: 추가 외 아이콘은 동작이 정해지지 않아 아직 클릭을 받지 않는다.
private enum class SpiritMenuType { ADD, FRIENDSHIP, SPIRIT, EMPTY }

private data class SpiritMenuIcon(
    val type: SpiritMenuType,
    @param:DrawableRes val iconRes: Int,
    val offsetX: Dp,
    val offsetY: Dp
)

@Composable
fun SpiritStage(
    spiritName: String,
    message: String,
    isMenuVisible: Boolean,
    onSpiritClick: () -> Unit,
    onMenuAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 하단 버튼이 내비게이션 바 위에 놓이므로 정령 위치도 같은 기준으로 맞춘다.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val spiritBottomPadding = SpiritBottomPadding

        val minX = with(density) { StageHorizontalMargin.toPx() }
        val maxX = with(density) { (maxWidth - StageHorizontalMargin - SpiritSize).toPx() }.coerceAtLeast(minX)
        val stageWidthPx = with(density) { maxWidth.toPx() }

        val spiritX = remember { Animatable((minX + maxX) / 2f) }
        var isMovingRight by remember { mutableStateOf(true) }

        // 아이콘 메뉴가 떠 있는 동안에는 이동을 멈추고, 닫히면 그 자리에서 다시 움직인다.
        LaunchedEffect(minX, maxX, isMenuVisible) {
            spiritX.snapTo(spiritX.value.coerceIn(minX, maxX))
            if (maxX <= minX || isMenuVisible) return@LaunchedEffect

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
                menuProgress.animateTo(0f, tween(MENU_CLOSE_DURATION_MS, easing = FastOutSlowInEasing))
            }
        }

        // 꼬리가 정령을 가리키도록 정령을 따라 움직이되, 좌우 여백 밖으로는 나가지 않는다.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = spiritBottomPadding + SpiritSize - BubbleOverlapSpirit)
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
                    scaleX = if (isMovingRight == SPIRIT_FACES_LEFT) -1f else 1f
                }
                .noRippleClickable {
                    onSpiritClick()
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
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            )
        }

        if (isMenuVisible || menuProgress.value > 0f) {
            val spiritHalfPx = with(density) { (SpiritSize / 2).toPx() }
            val iconHalfPx = with(density) { (MenuIconSize / 2).toPx() }
            // 정령이 화면 끝에 있어도 아이콘이 좌우 여백 밖으로 나가지 않도록 아이콘 묶음 중심을 제한한다.
            val menuHalfWidthPx = with(density) { SpiritMenuIcons.maxOf { abs(it.offsetX.value) }.dp.toPx() } + iconHalfPx
            val minCenterX = minX + menuHalfWidthPx
            val maxCenterX = (stageWidthPx - minX - menuHalfWidthPx).coerceAtLeast(minCenterX)

            SpiritMenuIcons.forEach { icon ->
                val onClick: (() -> Unit)? = when (icon.type) {
                    SpiritMenuType.ADD -> onMenuAddClick
                    else -> null
                }

                Image(
                    painter = painterResource(icon.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // 아이콘 중심을 정령 박스 중심 높이에 맞춘 뒤, 진행도만큼 각 위치로 이동한다.
                        .padding(bottom = spiritBottomPadding + SpiritSize / 2 - MenuIconSize / 2)
                        .offset {
                            val progress = menuProgress.value
                            val centerX = (spiritX.value + spiritHalfPx).coerceIn(minCenterX, maxCenterX)
                            IntOffset(
                                x = (centerX - iconHalfPx + icon.offsetX.toPx() * progress).roundToInt(),
                                y = (icon.offsetY.toPx() * progress).roundToInt()
                            )
                        }
                        .size(MenuIconSize)
                        .graphicsLayer {
                            val progress = menuProgress.value
                            val scale = MENU_ICON_START_SCALE + (1f - MENU_ICON_START_SCALE) * progress
                            scaleX = scale
                            scaleY = scale
                            alpha = progress.coerceIn(0f, 1f)
                        }
                        // 닫히는 중에는 누르지 못하게 한다.
                        .then(
                            if (onClick != null && isMenuVisible) Modifier.noRippleClickable(onClick = onClick)
                            else Modifier
                        )
                )
            }
        }
    }
}
