package com.example.todowithspirits.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf { spiritsLightColor }

object SpiritTodoTheme {
    val color: SpiritColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current
}

@Composable
fun SpiritTodoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColors provides spiritsLightColor
    ) {
        content()
    }
}