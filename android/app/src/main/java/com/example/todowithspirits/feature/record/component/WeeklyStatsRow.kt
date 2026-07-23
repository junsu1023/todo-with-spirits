package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun WeeklyStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                .padding(vertical = 14.dp, horizontal = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.weekly_achieved_plan),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                // dummy
                Text(
                    text = "999개",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.todoTextMain
                )

                Text(
                    text = " / 1000",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor8
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                .padding(vertical = 14.dp, horizontal = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.weekly_avg_rate),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "100%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }
        }
    }
}