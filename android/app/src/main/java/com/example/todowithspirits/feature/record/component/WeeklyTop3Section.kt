package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun WeeklyTop3Section() {
    val trackColor = SpiritTodoTheme.colors.onSurfaceColor3
    val items = listOf(
        Triple(trackColor, "학업/커리어", "15회"),
        Triple(trackColor, "학업/커리어", "15회"),
        Triple(trackColor, "인간관계/약속", "8회"),
        Triple(trackColor, "취미", "5회")
    )

    items.forEachIndexed { index, (color, name, count) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .background(color)
                )

                Spacer(Modifier.width(10.dp))

                Column {
                    if (index == 0) {
                        Text(
                            text = stringResource(R.string.top1),
                            fontSize = 12.sp,
                            color = SpiritTodoTheme.colors.onSurfaceColor7
                        )
                    }

                    Text(
                        text = name,
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )
                }
            }
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.colors.mainTextColor
            )
        }

        if (index < items.lastIndex) {
            Spacer(Modifier.height(6.dp))
        }
    }
}