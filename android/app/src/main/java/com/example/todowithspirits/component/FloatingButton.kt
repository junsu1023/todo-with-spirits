package com.example.todowithspirits.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FloatingButton(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fabShape = RoundedCornerShape(percent = 50)

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .sharedBounds(
                    rememberSharedContentState(key = QuickAddSharedKeys.CONTAINER),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(fabShape)
                )
                .size(QuickAddFabSize)
                .clip(fabShape)
                .background(SpiritTodoTheme.color.surfaceColor1)
                .border(
                    width = 1.dp,
                    color = SpiritTodoTheme.color.mainTextAndStroke,
                    shape = fabShape
                )
                .throttleClickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.todo_plus),
                colorFilter = ColorFilter.tint(SpiritTodoTheme.color.mainTextAndStroke),
                contentDescription = null,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = QuickAddSharedKeys.ICON),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        }
    }
}
