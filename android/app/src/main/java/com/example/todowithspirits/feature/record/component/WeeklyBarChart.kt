package com.example.todowithspirits.feature.record.component

import android.graphics.BlurMaskFilter
import android.graphics.Color.argb
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.WeeklyDailyChart
import com.example.todowithspirits.R
import com.example.todowithspirits.component.rememberAnimatedProgress
import com.example.todowithspirits.theme.SpiritTodoTheme

private val MIN_BAR_HEIGHT = 3.dp
private val MAX_BAR_HEIGHT = 100.dp
private val CHART_AREA_HEIGHT = MAX_BAR_HEIGHT + 24.dp
private val TOOLTIP_CORNER = 16.dp
private val TOOLTIP_TAIL_W = 13.dp
private val TOOLTIP_TAIL_H = 12.dp
private val TOOLTIP_WIDTH = 124.dp

private data class WeeklyBar(
    val dayOfMonth: Int,
    val dayLabel: String,
    val value: Int,
    val chart: WeeklyDailyChart
)

@Composable
fun WeeklyBarChart(charts: List<WeeklyDailyChart>) {
    if (charts.isEmpty()) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
        return
    }

    val bars = remember(charts) {
        charts.map { chart ->
            WeeklyBar(
                dayOfMonth = chart.date.dayOfMonth,
                dayLabel = chart.dayOfWeek,
                value = chart.scheduleCompleted + chart.routineCompleted,
                chart = chart
            )
        }
    }

    val maxValueRaw = bars.maxOf { it.value }
    val maxValue = maxValueRaw.coerceAtLeast(1)
    val crownIndex = if (maxValueRaw > 0) bars.indexOfFirst { it.value == maxValueRaw } else -1
    var pressedIndex by remember { mutableIntStateOf(-1) }

    Column(Modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_AREA_HEIGHT)
        ) {
            val colWidth = maxWidth / bars.size

            Row(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                bars.forEachIndexed { index, bar ->
                    val fraction = bar.value.toFloat() / maxValue
                    val animatedFraction by rememberAnimatedProgress(fraction, label = "barFraction$index")
                    val barHeight = (MAX_BAR_HEIGHT * animatedFraction).coerceAtLeast(MIN_BAR_HEIGHT)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(index) {
                                detectTapGestures(
                                    onPress = {
                                        pressedIndex = index
                                        tryAwaitRelease()
                                        pressedIndex = -1
                                    }
                                )
                            },
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
                                    text = "${bar.value}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpiritTodoTheme.color.onSurfaceColor3,
                                    modifier = Modifier.offset(y = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "${bar.value}",
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

            if (pressedIndex != -1) {
                val bar = bars[pressedIndex]
                val pressedFraction = bar.value.toFloat() / maxValue
                val pressedBarHeight = (MAX_BAR_HEIGHT * pressedFraction).coerceAtLeast(MIN_BAR_HEIGHT)
                val barRightX = colWidth * pressedIndex + colWidth * 0.75f
                val barLeftX = colWidth * pressedIndex + colWidth * 0.25f
                val tipY = (CHART_AREA_HEIGHT - pressedBarHeight + 4.dp)
                    .coerceIn(0.dp, CHART_AREA_HEIGHT)

                // 기본: 우측 상단. 우측 공간 부족하면 좌측 + 말꼬리 좌우 반전.
                val rightX = barRightX - TOOLTIP_CORNER
                val fitsRight = rightX + TOOLTIP_WIDTH <= maxWidth
                val tailOnLeft = fitsRight
                val tooltipX = if (fitsRight) {
                    rightX
                } else {
                    barLeftX + TOOLTIP_CORNER - TOOLTIP_WIDTH
                }.coerceIn(0.dp, (maxWidth - TOOLTIP_WIDTH).coerceAtLeast(0.dp))

                BarTooltipBubble(
                    scheduleCompleted = bar.chart.scheduleCompleted,
                    scheduleTotal = bar.chart.scheduleTotal,
                    routineCompleted = bar.chart.routineCompleted,
                    routineTotal = bar.chart.routineTotal,
                    tailOnLeft = tailOnLeft,
                    modifier = Modifier
                        .offset(x = tooltipX)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                            )
                            layout(placeable.width, placeable.height) {
                                placeable.place(0, tipY.roundToPx() - placeable.height)
                            }
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SpiritTodoTheme.color.onSurfaceColor8)
        )

        Spacer(Modifier.height(6.dp))

        // 일자 + 요일
        Row(Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bar.dayOfMonth.toString().padStart(2, '0'),
                        fontSize = 12.sp,
                        color = SpiritTodoTheme.color.onSurfaceColor8
                    )
                    Text(
                        text = bar.dayLabel,
                        fontSize = 10.sp,
                        color = SpiritTodoTheme.color.onSurfaceColor8
                    )
                }
            }
        }
    }
}

/**
 * 아래로 꼬리가 달린 말풍선. 꼬리는 하단 모서리에서 기울어지는 형태(한쪽 변은 수직, 반대쪽은 대각선).
 *
 * @param tailOnLeft true면 꼬리가 좌측 하단(말풍선이 막대 우측에 있을 때), false면 좌우 반전.
 */
@Composable
fun BarTooltipBubble(
    scheduleCompleted: Int,
    scheduleTotal: Int,
    routineCompleted: Int,
    routineTotal: Int,
    modifier: Modifier = Modifier,
    tailOnLeft: Boolean = true
) {
    val bubbleColor = SpiritTodoTheme.color.surfaceColor1

    Column(
        modifier = modifier
            .drawBehind {
                val cr = TOOLTIP_CORNER.toPx()
                val tailW = TOOLTIP_TAIL_W.toPx()
                val tailH = TOOLTIP_TAIL_H.toPx()
                val bodyBottom = (size.height - tailH).coerceAtLeast(0f)

                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = bodyBottom,
                            cornerRadius = CornerRadius(cr, cr)
                        )
                    )

                    if (tailOnLeft) {
                        // 좌측 하단: 왼쪽 변 수직, 팁에서 오른쪽 위로 대각선
                        val baseX = cr
                        moveTo(baseX, bodyBottom)
                        lineTo(baseX, size.height)
                        lineTo(baseX + tailW, bodyBottom)
                    } else {
                        // 우측 하단(좌우 반전): 오른쪽 변 수직, 팁에서 왼쪽 위로 대각선
                        val baseX = size.width - cr
                        moveTo(baseX, bodyBottom)
                        lineTo(baseX, size.height)
                        lineTo(baseX - tailW, bodyBottom)
                    }
                    close()
                }

                drawIntoCanvas { canvas ->
                    val shadowPaint = Paint().apply {
                        isAntiAlias = true
                        color = argb(16, 0, 0, 0)
                        maskFilter = BlurMaskFilter(
                            6.dp.toPx(),
                            BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.translate(2.dp.toPx(), 2.dp.toPx())
                    canvas.nativeCanvas.drawPath(path.asAndroidPath(), shadowPaint)
                    canvas.nativeCanvas.restore()
                }

                drawPath(path, bubbleColor)
            }
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 10.dp,
                bottom = 10.dp + TOOLTIP_TAIL_H
            )
    ) {
        Row(
            modifier = Modifier.width(96.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.todo),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.keyTodo,
                modifier = Modifier.alignByBaseline()
            )

            Text(
                text = "$scheduleCompleted / $scheduleTotal",
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.onSurfaceColor8,
                modifier = Modifier.alignByBaseline()
            )
        }

        Row(
            modifier = Modifier.width(96.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.routine),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.keyRoutine,
                modifier = Modifier.alignByBaseline()
            )

            Text(
                text = "$routineCompleted / $routineTotal",
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.onSurfaceColor8,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}