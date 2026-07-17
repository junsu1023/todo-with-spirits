package com.example.todowithspirits.component

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = null,
        indication = null,
        enabled = enabled,
        onClick = onClick
    )
