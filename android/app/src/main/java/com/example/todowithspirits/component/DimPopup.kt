package com.example.todowithspirits.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.todowithspirits.theme.SpiritTodoTheme

// 화면 위에 그리는 팝업. 어두운 배경이나 시스템 뒤로가기를 누르면 닫히고, 아래 화면으로는 터치가 가지 않는다.
@Composable
fun DimPopup(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.dimColor)
            .pointerInput(Unit) { detectTapGestures { currentOnDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        // 카드 안을 누른 터치가 배경까지 올라가 팝업을 닫지 않도록 소비한다.
        Box(modifier = Modifier.pointerInput(Unit) { detectTapGestures {} }) {
            content()
        }
    }
}
