package com.example.todowithspirits.feature.splash

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.splash.viewmodel.SplashViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel = hiltViewModel(),
    onSplashFinished: (isLoggedIn: Boolean) -> Unit,
) {
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsStateWithLifecycle()
    var minDurationPassed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000L.milliseconds)
        minDurationPassed = true
    }

    LaunchedEffect(isLoggedIn, minDurationPassed) {
        if (minDurationPassed) {
            isLoggedIn?.let(onSplashFinished)
        }
    }

    SplashContent()
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1),
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()
        val started = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { started.value = true }
        val contentAlpha by animateFloatAsState(
            targetValue = if(started.value) 1f else 0f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            label = "contentAlpha",
        )
        val transition = rememberInfiniteTransition(label = "spiritFloat")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "progress",
        )

        spiritFloatSpecs.forEach { spec ->
            FloatingSpirit(
                spec = spec,
                progress = progress,
                alpha = contentAlpha,
                canvasWidthPx = canvasWidthPx,
                canvasHeightPx = canvasHeightPx,
            )
        }

        SplashTitle(
            baseAlpha = contentAlpha,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

/**
 * 화면 위를 정해진 앵커 주변에서 두 개의 사인파(가로/세로 주파수가 다름)로 그리는 리사주 곡선을 따라
 * 떠다니는 정령. 회전과 크기도 함께 미세하게 흔들려 살아 있는 느낌을 준다.
 */
@Composable
private fun FloatingSpirit(
    spec: SpiritFloatSpec,
    progress: Float,
    alpha: Float,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) {
    Image(
        painter = painterResource(spec.drawable),
        contentDescription = null,
        modifier = Modifier
            .size(spec.size)
            .graphicsLayer {
                val angle = (2.0 * PI).toFloat() * progress
                val centerX = canvasWidthPx *
                    (spec.anchorX + spec.swingX * sin(angle * spec.freqX + spec.phase))
                val centerY = canvasHeightPx *
                    (spec.anchorY + spec.swingY * sin(angle * spec.freqY + spec.phase * 1.3f))

                translationX = centerX - size.width / 2f
                translationY = centerY - size.height / 2f
                rotationZ = spec.rotationSwing * sin(angle + spec.phase)

                val pulse = 0.9f + 0.1f * sin(angle * 2f + spec.phase)
                scaleX = pulse
                scaleY = pulse
                this.alpha = alpha
            },
    )
}

/**
 * 가운데 앱 이름. 정식 이름이 정해지기 전까지는 "Spirits"를 노출한다.
 * 배경의 정령들과 어울리도록 위아래로 부드럽게 떠오르고(bob), 숨 쉬듯 커졌다 작아지며(breathe),
 * 은은하게 밝기가 오르내린다(shimmer).
 */
@Composable
private fun SplashTitle(
    baseAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "splashTitle")
    val bob by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val shimmer by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer",
    )

    Text(
        text = stringResource(R.string.splash_app_name),
        modifier = modifier.graphicsLayer {
            translationY = bob
            scaleX = breathe
            scaleY = breathe
            alpha = shimmer * baseAlpha
        },
        style = TextStyle(
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp,
            color = SpiritTodoTheme.color.mainTextAndStroke,
        ),
    )
}

/**
 * @property anchorX 가로 기준 위치(0~1, 화면 폭 비율)
 * @property anchorY 세로 기준 위치(0~1, 화면 높이 비율)
 * @property swingX 앵커 기준 가로 진폭(화면 폭 비율)
 * @property swingY 앵커 기준 세로 진폭(화면 높이 비율)
 * @property freqX 가로 사인파 주파수(정수여야 주기 경계에서 위치가 튀지 않음)
 * @property freqY 세로 사인파 주파수(정수)
 * @property phase 정령별 위상차(라디안)
 * @property rotationSwing 좌우로 흔들리는 최대 각도(도)
 */
private data class SpiritFloatSpec(
    @DrawableRes val drawable: Int,
    val size: Dp,
    val anchorX: Float,
    val anchorY: Float,
    val swingX: Float,
    val swingY: Float,
    val freqX: Int,
    val freqY: Int,
    val phase: Float,
    val rotationSwing: Float,
)

private val spiritFloatSpecs = listOf(
    SpiritFloatSpec(
        drawable = R.drawable.wind_spirit,
        size = 96.dp,
        anchorX = 0.24f,
        anchorY = 0.26f,
        swingX = 0.30f,
        swingY = 0.34f,
        freqX = 2,
        freqY = 3,
        phase = 0f,
        rotationSwing = 22f,
    ),
    SpiritFloatSpec(
        drawable = R.drawable.fire_spirit,
        size = 104.dp,
        anchorX = 0.76f,
        anchorY = 0.32f,
        swingX = 0.28f,
        swingY = 0.30f,
        freqX = 3,
        freqY = 2,
        phase = 1.9f,
        rotationSwing = 16f,
    ),
    SpiritFloatSpec(
        drawable = R.drawable.ground_spirit,
        size = 118.dp,
        anchorX = 0.5f,
        anchorY = 0.78f,
        swingX = 0.34f,
        swingY = 0.22f,
        freqX = 2,
        freqY = 1,
        phase = 3.6f,
        rotationSwing = 12f,
    ),
)
