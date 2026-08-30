package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.domain.model.WeeklyDailyChart
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun WeeklyRecordRow(charts: List<WeeklyDailyChart>) {
    val statuses = charts.map { (it.scheduleTotal + it.routineTotal) to (it.scheduleCompleted + it.routineCompleted) }

    Row(modifier = Modifier.fillMaxWidth()) {
        statuses.forEachIndexed { index, status ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = SpiritTodoTheme.color.surfaceColor1,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = SpiritTodoTheme.color.surfaceColor15,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (status.first) {
                        0 -> { /* no icon */ }
                        status.second -> Image(
                            painter = painterResource(R.drawable.todo_important_24),
                            contentDescription = null
                        )
                        else -> Image(
                            painter = painterResource(R.drawable.todo_cross),
                            contentDescription = null
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${index + 1}일차",
                    fontSize = 10.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor8
                )
            }

            if (index != statuses.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(SpiritTodoTheme.color.surfaceColor15)
                    )
                }
            }
        }
    }
}