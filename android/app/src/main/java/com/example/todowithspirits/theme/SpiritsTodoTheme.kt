package com.example.todowithspirits.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LocalColors = staticCompositionLocalOf { spiritsLightColor }

object SpiritTodoTheme {
    val color: SpiritColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current
}

// 디자인이 기준으로 삼는 폭. 피그마 프레임을 이 값(dp)에 맞춰두면 1 피그마 px = 1dp로 그대로 옮길 수 있다.
private const val REFERENCE_WIDTH_DP = 360f

// LocalDensity 자체를 화면 폭 비율만큼 바꿔치기해서, 화면 전역의 모든 .dp 값이
// 별도 처리 없이 자동으로 기준 화면(360dp) 대비 같은 비율로 스케일되게 한다.
// fontScale은 그대로 두어(상쇄하지 않음) sp도 같은 비율로 함께 커지게 한다 - 디자이너의 피그마 가이드가
// 360dp 기준 14sp / 1080px 기준 42sp로, 폰트도 다른 값과 동일한 비율로 커지도록 되어 있기 때문이다.
// 시스템 접근성 글자 크기 설정(baseDensity.fontScale)은 그대로 곱해져 남아있어 이 스케일과 별개로 계속 반영된다.
// border 두께처럼 스케일을 피하고 싶은 값이 있다면 그 하위에서 LocalDensity를 원래 값으로 다시 제공하면 된다.
@Composable
private fun rememberScaledDensity(): Density {
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val scaleFactor = configuration.screenWidthDp / REFERENCE_WIDTH_DP

    return remember(baseDensity, scaleFactor) {
        Density(
            density = baseDensity.density * scaleFactor,
            fontScale = baseDensity.fontScale
        )
    }
}

@Composable
fun SpiritTodoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColors provides spiritsLightColor,
        LocalDensity provides rememberScaledDensity()
    ) {
        content()
    }
}