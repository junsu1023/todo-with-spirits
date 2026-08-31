package com.example.todowithspirits.feature.today.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.feature.today.state.SpiritInfo
import com.example.todowithspirits.theme.HexagonShape
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SpiritSection(spiritInfo: SpiritInfo) {
    val expProgress = if (spiritInfo.maxExp == 0) 0f else spiritInfo.curExp.toFloat() / spiritInfo.maxExp.toFloat()
    val progress by rememberAnimatedProgress(expProgress, label = "spiritExpProgress")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Note: 대표 정령 기능 추가 시 데이터 가져와서 띄우도록 수정 예정
        Image(
            painter = painterResource(R.drawable.fire_spirit),
            contentDescription = null,
            modifier = Modifier.size(110.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "벌써 절반 왔어요!\n루미랑 조금 더 힘내봐요 :)",
                color = SpiritTodoTheme.color.mainTextAndStroke,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = spiritInfo.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SpiritTodoTheme.color.onSurfaceColor5
                    )

                    Spacer(Modifier.width(2.dp))

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(SpiritTodoTheme.color.mainArea, HexagonShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${spiritInfo.level}",
                            color = SpiritTodoTheme.color.onSurfaceColor3,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Text(
                    text = "${spiritInfo.curExp} / ${spiritInfo.maxExp}",
                    fontSize = 10.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )
            }

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SpiritTodoTheme.color.surfaceColor5)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SpiritTodoTheme.color.mainArea)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .width(150.dp)
                    .height(21.dp)
                    .background(SpiritTodoTheme.color.surfaceColor6, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.todo_flame),
                    colorFilter = ColorFilter.tint(SpiritTodoTheme.color.mainTextAndStroke),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "오늘의 성장 포인트 +${spiritInfo.todayPoints}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.mainTextAndStroke
                )
            }
        }
    }
}