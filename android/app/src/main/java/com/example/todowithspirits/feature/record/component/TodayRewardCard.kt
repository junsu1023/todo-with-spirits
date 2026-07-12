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

private data class RewardItem(
    val iconRes: Int,
    val type: String,
    val description: String,
    val exp: Int,
    val isHidden: Boolean = false
)

private val dummyRewards = listOf(
    RewardItem(R.drawable.fi_rr_time_check, "일일 미션", "오늘 플랜 5개 이상 완료", 20),
    RewardItem(R.drawable.fi_rr_fire, "끄기 스코어", "미뤘던 목표 2개 완료", 20),
    RewardItem(R.drawable.important_icon, "히든 미션", "정령의 호감도 +10 쌓기", 100, isHidden = true),
    RewardItem(R.drawable.important_icon, "히든 미션", "정령의 호감도 +10 쌓기", 100, isHidden = true)
)

@Composable
fun TodayRewardCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.today_reward),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Spacer(Modifier.height(12.dp))

        dummyRewards.forEachIndexed { index, reward ->
            RewardRow(reward = reward)

            if (index < dummyRewards.lastIndex) {
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
                    .background(SpiritTodoTheme.colors.onSurfaceColor7, RoundedCornerShape(30.dp))
                    .padding(horizontal = 24.dp, vertical = 8.5.dp)
            ) {
                Text(
                    text = stringResource(R.string.more),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.colors.white
                )
            }
        }
    }
}

@Composable
private fun RewardRow(reward: RewardItem) {
    val accentColor = if(reward.isHidden) SpiritTodoTheme.colors.onSurfaceColor1 else SpiritTodoTheme.colors.onSurfaceColor7

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.white)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(reward.iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reward.type,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )

            Spacer(Modifier.height(1.dp))

            Text(
                text = reward.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = reward.exp.toString(),
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
