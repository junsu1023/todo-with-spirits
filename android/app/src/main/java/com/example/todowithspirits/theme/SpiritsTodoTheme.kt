package com.example.todowithspirits.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf { spiritsTodoColor }
val LocalLightColors = staticCompositionLocalOf { spiritsLightColor }

object SpiritTodoTheme {
    val colors: SpiritsTodoColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val color: SpiritColor
        @Composable
        @ReadOnlyComposable
        get() = LocalLightColors.current
}

@Composable
fun SpiritTodoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColors provides spiritsTodoColor,
        LocalLightColors provides spiritsLightColor
    ) {
        content()
    }
}