package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthlyCategoryCount
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun MissedAreaRow(bottomCategory: MonthlyCategoryCount = MonthlyCategoryCount("NONE", 0, 0)) {
    val missedCount = (bottomCategory.totalCount - bottomCategory.completedCount).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = bottomCategory.category.toCategoryDisplayName(),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Text(
            text = "${missedCount}회",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.color.todoTextMain
        )
    }
}