package com.example.todowithspirits.feature.mypage.component

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.theme.HexagonShape
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlin.math.roundToInt

@Composable
fun MySpiritCard(
    expCurrent: Int = 999,
    expMax: Int = 1000
) {
    val expProgress = if (expMax == 0) 0f else expCurrent.toFloat() / expMax
    val animatedExpProgress by rememberAnimatedProgress(expProgress, label = "expProgress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.surfaceColor10, RoundedCornerShape(6.dp))
            .padding(top = 16.dp, bottom = 14.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.my_spirit),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.temp_spirit),
                contentDescription = null,
                modifier = Modifier.size(85.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(SpiritTodoTheme.color.mainBackground, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.representative_spirit),
                        fontSize = 10.sp,
                        color = SpiritTodoTheme.color.mainTextAndStroke,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "루미", // dummy
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.color.todoTextMain
                    )

                    Spacer(Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(SpiritTodoTheme.color.mainArea, HexagonShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3", // dummy
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpiritTodoTheme.color.onSurfaceColor3
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.exp_format, expCurrent, expMax),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = SpiritTodoTheme.color.todoTextMain
                    )
                    Text(
                        text = "${(animatedExpProgress * 100).roundToInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.color.mainArea
                    )
                }

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SpiritTodoTheme.color.surfaceColor5)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedExpProgress.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(SpiritTodoTheme.color.mainArea)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                    .noRippleClickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.view_spirit),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                    .noRippleClickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.change_representative),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }
        }
    }
}