package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.domain.model.RecordTaskItem
import com.example.todowithspirits.R
import com.example.todowithspirits.component.CircularProgressArc
import com.example.todowithspirits.component.SpeechBubble
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlin.math.roundToInt

@Composable
fun DailyReportCard(
    achievementRate: Float,
    todoTotalCnt: Int,
    todoCompCnt: Int,
    routineTotalCnt: Int,
    routineCompCnt: Int,
    items: List<RecordTaskItem>
) {
    val progress by rememberAnimatedProgress(achievementRate, label = "achievementRate")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(12.dp))
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
                    color = SpiritTodoTheme.color.mainTextAndStroke
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "루미랑 남은 4개도 끝내볼까요?",
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }

            Image(
                painter = painterResource(R.drawable.todo_share),
                contentDescription = null
            )
        }

        Spacer(Modifier.height(20.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val trackWidth = maxWidth
            val badgeWidth = 55.dp
            val badgeOffsetX = trackWidth * progress - badgeWidth / 2

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(169.dp))
                    .background(SpiritTodoTheme.color.surfaceColor1)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(169.dp))
                        .background(SpiritTodoTheme.color.mainArea)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = badgeOffsetX)
                    .width(badgeWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(211.dp))
                        .border(
                            1.dp,
                            SpiritTodoTheme.color.mainTextAndStroke,
                            RoundedCornerShape(211.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        fontSize = 12.sp,
                        color = SpiritTodoTheme.color.mainTextAndStroke,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(4.dp))

                Image(
                    painter = painterResource(R.drawable.fire_spirit),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = stringResource(R.string.today_progressing, (todoCompCnt + routineCompCnt), (todoTotalCnt + routineTotalCnt)),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.mainTextAndStroke
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
                progress = if(todoTotalCnt == 0) 0f else todoCompCnt / todoTotalCnt.toFloat(),
                progressColor = SpiritTodoTheme.color.keyTodo,
                countText = "$todoCompCnt / $todoTotalCnt"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                label = "루틴",
                progress = if(routineTotalCnt == 0) 0f else routineCompCnt / routineTotalCnt.toFloat(),
                progressColor = SpiritTodoTheme.color.keyRoutine,
                countText = "$routineCompCnt / $routineTotalCnt"
            )
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(53.dp)
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.goal_to_try_again_tomorrow),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.todoTextMain
                )

                Text(
                    text = "${items.count { !it.completed } }",
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain,
                    fontWeight = FontWeight.Medium
                )
            }

            SpeechBubble(
                text = stringResource(R.string.goal_to_try_again_tomorrow_desc),
                backgroundColor = SpiritTodoTheme.color.mainArea,
                textColor = SpiritTodoTheme.color.onSurfaceColor3,
                tailHorizontalBias = 0.85f,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 40.dp)
                    .offset(y = (-20).dp)
                    .zIndex(1f)
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
    val trackColor = SpiritTodoTheme.color.systemBackground
    val animatedProgress by rememberAnimatedProgress(progress, label = "statCardProgress")

    Column(
        modifier = modifier
            .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressArc(
                progress = animatedProgress,
                color = progressColor,
                trackColor = trackColor,
                strokeWidth = 4.dp,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "${(animatedProgress * 100).roundToInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = progressColor
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = countText,
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.onSurfaceColor8
        )
    }
}
