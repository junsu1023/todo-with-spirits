package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MissionType
import com.example.domain.model.RecordReward
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun TodayRewardCard(todayRewards: List<RecordReward>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.today_reward),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Spacer(Modifier.height(12.dp))

        todayRewards.forEachIndexed { index, reward ->
            RewardRow(reward = reward)

            if (index < todayRewards.lastIndex) {
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(SpiritTodoTheme.color.onSurfaceColor8, RoundedCornerShape(30.dp))
                    .padding(horizontal = 24.dp, vertical = 8.5.dp)
            ) {
                Text(
                    text = stringResource(R.string.more),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.onSurfaceColor3
                )
            }
        }
    }
}

@Composable
private fun RewardRow(reward: RecordReward) {
    val accentColor = if(reward.missionType == MissionType.HIDDEN.name) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.onSurfaceColor8

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.surfaceColor1)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(reward.iconType.typeToRes()),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reward.missionType.translate(),
                fontSize = 12.sp,
                color = accentColor
            )

            Spacer(Modifier.height(1.dp))

            Text(
                text = reward.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = reward.rewardExp.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )

            Text(
                text = stringResource(R.string.exp),
                fontSize = 10.sp,
                color = accentColor
            )
        }
    }
}

private fun String.typeToRes(): Int {
    return when(this) {
        "THUMB_UP" -> R.drawable.todo_thumb_up
        "FLAME" -> R.drawable.todo_flame
        else -> R.drawable.todo_diamond
    }
}

private fun String.translate(): String {
    return when(this) {
        MissionType.DAILY.name -> "일일 미션"
        MissionType.CONSISTENCY.name -> "끈기 스코어"
        MissionType.HIDDEN.name -> "히든 미션"
        else -> "기타"
    }
}
