package com.example.todowithspirits.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberAnimatedProgress(
    targetValue: Float,
    durationMillis: Int = 1000,
    label: String = "animatedProgress"
): State<Float> {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    return animateFloatAsState(
        targetValue = if (animationStarted) targetValue.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = label
    )
}
