package com.example.todowithspirits.component

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = null,
        indication = null,
        enabled = enabled,
        onClick = onClick
    )

private const val DEFAULT_THROTTLE_INTERVAL_MS = 500L

@Composable
fun rememberThrottledOnClick(
    intervalMs: Long = DEFAULT_THROTTLE_INTERVAL_MS,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTimeMs by remember { mutableLongStateOf(0L) }
    val currentOnClick by rememberUpdatedState(onClick)

    return {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTimeMs >= intervalMs) {
            lastClickTimeMs = now
            currentOnClick()
        }
    }
}

fun Modifier.throttleClickable(
    intervalMs: Long = DEFAULT_THROTTLE_INTERVAL_MS,
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val throttledOnClick = rememberThrottledOnClick(intervalMs, onClick)

    if (showRipple) {
        clickable(enabled = enabled, onClick = throttledOnClick)
    } else {
        clickable(
            interactionSource = null,
            indication = null,
            enabled = enabled,
            onClick = throttledOnClick
        )
    }
}