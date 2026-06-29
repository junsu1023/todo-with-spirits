package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun FloatingButton(navigateToAdd: () -> Unit) {
    FloatingActionButton(
        onClick = navigateToAdd,
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 1.dp,
                color = SpiritTodoTheme.colors.onSurfaceColor1,
                shape = CircleShape
            ),
        shape = CircleShape,
        containerColor = SpiritTodoTheme.colors.white
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_plus),
            contentDescription = null
        )
    }
}
