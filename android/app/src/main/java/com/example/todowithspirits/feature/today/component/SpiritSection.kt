package com.example.todowithspirits.feature.today.component

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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

// Dummy spirit/exp data
private const val SPIRIT_NAME = "루미"
private const val SPIRIT_LEVEL = 99
private const val CURRENT_EXP = 9999
private const val MAX_EXP = 9999
private const val TODAY_POINTS = 999

private val HexagonShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    moveTo(width * 0.5f, 0f)
    lineTo(width, height * 0.25f)
    lineTo(width, height * 0.75f)
    lineTo(width * 0.5f, height)
    lineTo(0f, height * 0.75f)
    lineTo(0f, height * 0.25f)
    close()
}

@Composable
fun SpiritSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.temp_app_icon),
            contentDescription = null,
            modifier = Modifier.size(110.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "벌써 절반 왔어요!\n루미랑 조금 더 힘내봐요 :)",
                color = SpiritTodoTheme.colors.textColor2,
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
                        text = SPIRIT_NAME,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )

                    Spacer(Modifier.width(2.dp))

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(SpiritTodoTheme.colors.selectedTabColor, HexagonShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$SPIRIT_LEVEL",
                            color = SpiritTodoTheme.colors.white,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Text(
                    text = "$CURRENT_EXP / $MAX_EXP",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.textColor1
                )
            }

            Spacer(Modifier.height(5.dp))

            LinearProgressIndicator(
                progress = { CURRENT_EXP.toFloat() / MAX_EXP.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(9999.dp)),
                color = SpiritTodoTheme.colors.onSurfaceColor1,
                trackColor = SpiritTodoTheme.colors.trackColor
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .width(150.dp)
                    .height(21.dp)
                    .background(SpiritTodoTheme.colors.surfaceColor1, RoundedCornerShape(4.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(6.dp))

                Image(
                    painter = painterResource(R.drawable.fi_rr_fire),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.selectedTabColor)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "오늘의 성장 포인트 +$TODAY_POINTS",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )
            }
        }
    }
}