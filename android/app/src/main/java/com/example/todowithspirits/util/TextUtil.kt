package com.example.todowithspirits.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/*
큰 숫자는 폰트 자체의 위아래 여백(ascent/descent) 때문에 피그마보다 훨씬 크게 잡힌다.
텍스트는 원래 크기로 그리되(잘리지 않게), 레이아웃에는 기준선을 하단에 맞춘 height만큼만 차지하도록 보고한다.
숫자와 '/'는 기준선 아래로 내려가는 부분이 거의 없어 하단 정렬만으로 피그마와 같은 모양이 된다.
 */
fun Modifier.baselineAtBottom(height: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    val boxHeight = height.roundToPx()
    val baseline = placeable[FirstBaseline]

    layout(placeable.width, boxHeight, mapOf(FirstBaseline to boxHeight)) {
        placeable.place(0, boxHeight - baseline)
    }
}