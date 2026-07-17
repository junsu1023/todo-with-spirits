package com.example.todowithspirits.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PillBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 10.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(50.dp))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
