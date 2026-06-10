package com.example.todowithspirits.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf { splitsTodoColor }

object SplitsTodoTheme {
    val colors: SplitsTodoColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current
}

@Composable
fun SplitsTodoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColors provides splitsTodoColor
    ) {
        content()
    }
}