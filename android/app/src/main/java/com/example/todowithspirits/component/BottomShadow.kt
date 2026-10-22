package com.example.todowithspirits.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.theme.SpiritTodoTheme

private const val BOTTOM_SHADOW_ALPHA = 0.04f

/*
위 영역 아래쪽에만 떨어지는 그림자 (피그마: y 16, blur 20, 검정 4%).
Modifier.shadow는 네 방향 모두 그려져 같은 배경색의 위쪽 영역과 경계가 생기므로, 아래 콘텐츠 위에 겹쳐 그린다.
 */
@Composable
fun BottomShadow(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp
) {
    val shadowColor = SpiritTodoTheme.color.dimColor.copy(alpha = BOTTOM_SHADOW_ALPHA)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(listOf(shadowColor, shadowColor.copy(alpha = 0f))))
    )
}
