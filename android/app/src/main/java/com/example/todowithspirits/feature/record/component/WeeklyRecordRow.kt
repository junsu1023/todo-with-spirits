package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun WeeklyRecordRow(statuses: List<WeekDayStatus>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEachIndexed { index, status ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = SpiritTodoTheme.colors.white,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = SpiritTodoTheme.colors.onSurfaceColor11,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (status) {
                        WeekDayStatus.COMPLETED -> Image(
                            painter = painterResource(R.drawable.fi_rr_color_star),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        WeekDayStatus.FAILED -> Image(
                            painter = painterResource(R.drawable.fi_rr_cross_small),
                            contentDescription = null
                        )
                        WeekDayStatus.EMPTY -> { /* no icon */ }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${index + 1}일차",
                    fontSize = 10.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor7
                )
            }
        }
    }
}