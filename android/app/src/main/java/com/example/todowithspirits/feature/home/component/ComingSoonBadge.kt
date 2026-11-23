package com.example.todowithspirits.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun ComingSoonBadge(modifier: Modifier = Modifier, gap: Dp = 8.dp) {
    Text(
        text = stringResource(R.string.coming_soon),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = SpiritTodoTheme.color.white,
        modifier = modifier
            .placeAboveParent(gap)
            .background(SpiritTodoTheme.color.font01, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

private fun Modifier.placeAboveParent(gap: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(Constraints())
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(
            x = (constraints.maxWidth - placeable.width) / 2,
            y = -placeable.height - gap.roundToPx()
        )
    }
}
