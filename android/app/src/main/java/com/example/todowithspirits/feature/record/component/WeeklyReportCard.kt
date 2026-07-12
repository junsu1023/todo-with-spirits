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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun WeeklyReportCard() {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(6.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.weekly_report_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpiritTodoTheme.color.onSurfaceColor4
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.weekly_report_subtitle),
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.color.todoTextMain
                    )
                }
                Image(
                    painter = painterResource(R.drawable.todo_share),
                    contentDescription = null
                )
            }

            Spacer(Modifier.height(28.dp))

            WeeklyBarChart()

            Spacer(Modifier.height(16.dp))

            WeeklyStatsRow()

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                    .padding(top = 9.dp, start = 14.dp, bottom = 16.dp, end = 11.dp)
            ) {
                Text(
                    text = stringResource(R.string.weekly_record),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.todoTextMain
                )

                Spacer(Modifier.height(10.dp))

                WeeklyRecordRow(dummyDayStatuses)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.weekly_analysis),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(20.dp))

            WeeklyAnalysisSection(todoFraction = 0.5f, routineFraction = 0.3f, delayFraction = 0.2f)

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.weekly_top3),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(6.dp))

            WeeklyTop3Section()

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.weekly_missed_area),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(6.dp))

            MissedAreaRow()
        }
    }
}