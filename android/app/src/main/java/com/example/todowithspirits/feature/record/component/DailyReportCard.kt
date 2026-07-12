package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun DailyReportCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "다 잘해 진짜!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "루미랑 남은 4개도 끝내볼까요?",
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }

            Image(
                painter = painterResource(R.drawable.fi_rr_sign_out),
                contentDescription = null
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth(0.62f)) {
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(SpiritTodoTheme.colors.white, RoundedCornerShape(211.dp))
                            .border(1.dp, SpiritTodoTheme.colors.onSurfaceColor1, RoundedCornerShape(211.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "60%",
                            fontSize = 12.sp,
                            color = SpiritTodoTheme.colors.onSurfaceColor1,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Image(
                        painter = painterResource(R.drawable.temp_spirit),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(169.dp))
                    .background(SpiritTodoTheme.colors.onSurfaceColor3)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(169.dp))
                        .background(SpiritTodoTheme.colors.onSurfaceColor1)
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = stringResource(R.string.today_progressing, 6, 10),
                fontSize = 12.sp,
                color = SpiritTodoTheme.colors.onSurfaceColor1
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.todo),
                progress = 1f,
                progressColor = SpiritTodoTheme.colors.onSurfaceColor4,
                countText = "2 / 5"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                label = "루틴",
                progress = 1f,
                progressColor = SpiritTodoTheme.colors.onSurfaceColor5,
                countText = "2 / 5"
            )
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .background(SpiritTodoTheme.colors.onSurfaceColor1, RoundedCornerShape(50.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.goal_to_try_again_tomorrow_desc),
                fontSize = 10.sp,
                color = SpiritTodoTheme.colors.white
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .background(SpiritTodoTheme.colors.white)
                .clip(RoundedCornerShape(6.dp))
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.goal_to_try_again_tomorrow),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Text(
                text = "5개",
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.todoTextMain,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    progress: Float,
    progressColor: Color,
    countText: String
) {
    val trackColor = SpiritTodoTheme.colors.onSurfaceColor3

    Column(
        modifier = modifier
            .background(SpiritTodoTheme.colors.white, RoundedCornerShape(8.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgress(
                progress = progress,
                color = progressColor,
                trackColor = trackColor,
                strokeWidth = 6.dp,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "100%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = progressColor
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = countText,
            fontSize = 12.sp,
            color = SpiritTodoTheme.colors.onSurfaceColor7
        )
    }
}

@Composable
fun CircularProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}