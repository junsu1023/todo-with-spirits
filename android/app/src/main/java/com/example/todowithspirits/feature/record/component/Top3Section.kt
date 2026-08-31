package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.WeeklyAchievement
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.R

@Composable
fun Top3Section(weeklyTop3Section: List<WeeklyAchievement> = emptyList()) {
    val items = weeklyTop3Section.ifEmpty { emptyList() }

    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        when(index) {
                            0 -> R.drawable.todo_rank1
                            1 -> R.drawable.todo_rank2
                            else -> R.drawable.todo_rank3
                        }
                    ),
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.color.todoTextMain
                    )
                }
            }

            Text(
                text = item.targetCount.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.color.todoTextMain
            )
        }

        if (index < items.lastIndex) {
            Spacer(Modifier.height(6.dp))
        }
    }
}