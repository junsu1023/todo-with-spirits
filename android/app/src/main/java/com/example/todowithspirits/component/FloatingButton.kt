package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.today.component.QuickAddBottomPopup
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun FloatingButton(
    isBottomSheetVisible: Boolean,
    setBottomSheetVisible: (Boolean) -> Unit
) {
    FloatingActionButton(
        onClick = { setBottomSheetVisible(true) },
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 1.dp,
                color = SpiritTodoTheme.color.mainTextAndStroke,
                shape = CircleShape
            ),
        shape = CircleShape,
        containerColor = SpiritTodoTheme.color.surfaceColor1
    ) {
        Image(
            painter = painterResource(R.drawable.todo_plus),
            colorFilter = ColorFilter.tint(SpiritTodoTheme.color.mainTextAndStroke),
            contentDescription = null
        )
    }

    if (isBottomSheetVisible) {
        QuickAddBottomPopup(onDismiss = { setBottomSheetVisible(false) })
    }
}
