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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import com.example.domain.model.MonthlyComparison
import com.example.todowithspirits.R
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlin.math.roundToInt

private data class MonthBarData(val month: Int, val value: Int?)

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
    val validValues = monthBarData.mapNotNull { it.value }
    val maxValue = if (validValues.isEmpty()) 1 else validValues.max().coerceAtLeast(1)
    val maxIndex = monthBarData.indexOfFirst { it.value != null && it.value == validValues.max() }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            monthBarData.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (index == maxIndex) {
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
                                text = "${monthBarData[maxIndex].value}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpiritTodoTheme.color.onSurfaceColor3,
                                modifier = Modifier.offset(y = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(Modifier.fillMaxWidth()) {
            monthBarData.forEach { month ->
                Text(
                    text = if (month.value == null) "?" else "${month.value}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            monthBarData.forEach { month ->
                val fraction = if (month.value == null || maxValue == 0) 0.05f else month.value.toFloat() / maxValue
                val animatedFraction by rememberAnimatedProgress(fraction, label = "monthBarFraction${month.month}")

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(animatedFraction.coerceAtLeast(0.01f))
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