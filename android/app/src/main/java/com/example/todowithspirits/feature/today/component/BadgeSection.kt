package com.example.todowithspirits.feature.today.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

// dummy data
private const val ACHIEVEMENT_RATE = 1f

@Composable
fun BadgeAndAchievementRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BadgeCard(modifier = Modifier.weight(2f))

        AchievementCard(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BadgeCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SpiritTodoTheme.colors.white,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .height(116.dp)
                .padding(start = 10.dp, top = 8.dp, end = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.growth_badge),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor2
                )

                Icon(
                    painter = painterResource(R.drawable.right),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = SpiritTodoTheme.colors.onSurfaceColor2
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // dummy data
                BadgePlaceholder("첫 루틴 시작")

                BadgePlaceholder("히든 업적")

                BadgePlaceholder("꾸준한 노력")
            }
        }
    }
}

@Composable
private fun BadgePlaceholder(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .border(1.dp, SpiritTodoTheme.colors.textColor1, CircleShape)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            color = SpiritTodoTheme.colors.mainTextColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AchievementCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SpiritTodoTheme.colors.white,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .height(116.dp)
                .padding(top = 8.dp)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.today_completion_rate),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor2
                )
            }

            Spacer(Modifier.height(12.dp))

            CircularProgressIndicator(
                progress = ACHIEVEMENT_RATE
            )
        }
    }
}

@Composable
private fun CircularProgressIndicator(progress: Float) {
    val onSurfaceColor1 = SpiritTodoTheme.colors.onSurfaceColor1
    val trackColor = SpiritTodoTheme.colors.trackColor2
    val percentage = (progress * 100).toInt()

    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = arcSize,
                topLeft = topLeft
            )

            drawArc(
                color = onSurfaceColor1,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
        }

        Text(
            text = "$percentage%",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceColor1
        )
    }
}