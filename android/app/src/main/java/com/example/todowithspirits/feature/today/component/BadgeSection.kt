package com.example.todowithspirits.feature.today.component

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.CircularProgressArc
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlin.math.roundToInt

// dummy data
private const val ACHIEVEMENT_RATE = 0.7f

@Composable
fun BadgeAndAchievementRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BadgeCard(modifier = Modifier.weight(2.5f))

        AchievementCard(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BadgeCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SpiritTodoTheme.color.surfaceColor1,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .height(120.dp)
                .padding(start = 10.dp, top = 8.dp, end = 6.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.growth_badge),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )

                Image(
                    painter = painterResource(R.drawable.todo_arrow2),
                    contentDescription = null
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
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
        Image(
            painter = painterResource(R.drawable.temp_badge),
            contentDescription = null
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            color = SpiritTodoTheme.color.systemGrey,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AchievementCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SpiritTodoTheme.color.surfaceColor1,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .height(120.dp)
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
                    color = SpiritTodoTheme.color.systemGrey
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
    val todoTextMain = SpiritTodoTheme.color.mainTextAndStroke
    val trackColor = SpiritTodoTheme.color.surfaceColor7

    val animatedProgress by rememberAnimatedProgress(progress, label = "achievementRateProgress")
    val percentage = (animatedProgress * 100).roundToInt()

    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressArc(
            progress = animatedProgress,
            color = todoTextMain,
            trackColor = trackColor,
            strokeWidth = 4.dp,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "$percentage%",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = todoTextMain
        )
    }
}