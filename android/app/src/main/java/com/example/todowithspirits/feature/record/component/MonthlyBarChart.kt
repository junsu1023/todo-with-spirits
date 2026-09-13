package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthlyComparison
import com.example.todowithspirits.R
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlin.math.roundToInt

private val MIN_BAR_HEIGHT = 3.dp
private val MAX_BAR_HEIGHT = 80.dp
private val CHART_AREA_HEIGHT = MAX_BAR_HEIGHT + 24.dp

private data class MonthBarData(val month: Int, val value: Int?) // value == null: 아직 오지 않은 미래 달 ("?" 표시)

@Composable
fun MonthlyBarChart(
    comparisons: List<MonthlyComparison> = emptyList(),
    currentMonth: Int = 12
) {
    val monthBarData = remember(comparisons, currentMonth) {
        (1..12).map { month ->
            val value = comparisons.find { it.month == month }
                ?.takeIf { month <= currentMonth }
                ?.let { (it.completedRate * 100).roundToInt() }
            MonthBarData(month, value)
        }
    }

    val maxValueRaw = monthBarData.mapNotNull { it.value }.maxOrNull() ?: 0
    val maxValue = maxValueRaw.coerceAtLeast(1)
    val crownIndex = if (maxValueRaw > 0) monthBarData.indexOfFirst { it.value == maxValueRaw } else -1

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_AREA_HEIGHT)
        ) {
            monthBarData.forEachIndexed { index, month ->
                val fraction = if (month.value == null) 0f else month.value.toFloat() / maxValue
                val animatedFraction by rememberAnimatedProgress(fraction, label = "monthBarFraction${month.month}")
                val barHeight = if (month.value == null) MIN_BAR_HEIGHT
                    else (MAX_BAR_HEIGHT * animatedFraction).coerceAtLeast(MIN_BAR_HEIGHT)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (index == crownIndex) {
                        Box(
                            modifier = Modifier.size(width = 24.dp, height = 19.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.fi_rr_crown),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )

                            Text(
                                text = "${month.value}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpiritTodoTheme.color.onSurfaceColor3,
                                modifier = Modifier.offset(y = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = month.value?.toString() ?: "?",
                            fontSize = 12.sp,
                            color = SpiritTodoTheme.color.todoTextMain
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(SpiritTodoTheme.color.systemArea)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SpiritTodoTheme.color.onSurfaceColor8)
        )

        Spacer(Modifier.height(5.dp))

        Row(Modifier.fillMaxWidth()) {
            monthBarData.forEach { month ->
                Text(
                    text = "${month.month}월",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor8
                )
            }
        }
    }
}
