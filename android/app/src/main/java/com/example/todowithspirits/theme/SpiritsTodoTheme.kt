package com.example.todowithspirits.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf { spiritsTodoColor }

object SpiritTodoTheme {
    val colors: SpiritsTodoColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current
}

@Composable
fun SpiritTodoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColors provides spiritsTodoColor
    ) {
        content()
    }
}