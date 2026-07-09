package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.YearMonth

private fun getSeasonTitle(month: Int): String = when (month) {
    1 -> "한겨울의"
    2 -> "겨울 끝의"
    3 -> "이른 봄의"
    4 -> "봄꽃의"
    5 -> "늦봄의"
    6 -> "초여름의"
    7 -> "한여름의"
    8 -> "여름 끝의"
    9 -> "초가을의"
    10 -> "가을의"
    11 -> "늦가을의"
    else -> "초겨울의"
}

@Composable
fun MonthlyReportCard() {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(6.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val yearShort = currentYearMonth.year % 100
                val season = getSeasonTitle(currentYearMonth.monthValue)

                Text(
                    text = "${yearShort}년 ${season} ${currentYearMonth.monthValue}월",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )

                Row {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_angle_small_left),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.mainTextColor),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { currentYearMonth = currentYearMonth.minusMonths(1) }
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Image(
                        painter = painterResource(R.drawable.fi_rr_angle_small_right),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.mainTextColor),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { currentYearMonth = currentYearMonth.plusMonths(1) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // dummy
            Text(
                text = "이번 달도 바쁘겠지만 할 수 있어요!",
                fontSize = 14.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(20.dp))

            MonthlyCalendar(yearMonth = currentYearMonth, today = today)

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(63.dp)
                        .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp)
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.monthly_this_month_achieved),
                        fontSize = 12.sp,
                        color = SpiritTodoTheme.colors.mainTextColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(5.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "15개",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpiritTodoTheme.colors.mainTextColor,
                            modifier = Modifier.alignByBaseline()
                        )

                        Text(
                            text = "/ 1000",
                            fontSize = 12.sp,
                            color = SpiritTodoTheme.colors.onSurfaceColor7,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(63.dp)
                        .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp)
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.monthly_avg_rate),
                        fontSize = 12.sp,
                        color = SpiritTodoTheme.colors.mainTextColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "99%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_comparison),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(18.dp))

            MonthlyBarChart()

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                    .border(1.dp, SpiritTodoTheme.colors.onSurfaceColor1, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.monthly_comparison_desc, 99),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_analysis),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(22.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.temp_spirit),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.fi_rr_star),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFFFFC107)),
                        modifier = Modifier
                            .size(52.dp)
                            .offset(x = 44.dp, y = (-28).dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.fi_rr_star),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFFFFC107)),
                        modifier = Modifier
                            .size(50.dp)
                            .offset(x = (-46).dp, y = (-20).dp)
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = (-60).dp, y = (5).dp)
                            .background(SpiritTodoTheme.colors.surfaceColor1, RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_achievement_rate, 99),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SpiritTodoTheme.colors.onSurfaceColor1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = ((-50).dp), y = 25.dp)
                            .background(SpiritTodoTheme.colors.surfaceColor1, RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_peer_top, 4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SpiritTodoTheme.colors.onSurfaceColor1
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.monthly_analysis_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiritTodoTheme.colors.onSurfaceColor12,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.monthly_analysis_subtitle),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.mainTextColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = stringResource(R.string.monthly_top3),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(6.dp))

            WeeklyTop3Section()

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.monthly_most_delayed),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(6.dp))

            MissedAreaRow()
        }
    }
}