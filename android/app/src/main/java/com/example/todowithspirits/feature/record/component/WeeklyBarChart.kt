package com.example.todowithspirits.feature.record.component

import android.graphics.BlurMaskFilter
import android.graphics.Color.argb
import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

enum class WeekDayStatus { COMPLETED, FAILED, EMPTY }

data class DayBarData(val date: Int, val dayLabel: String, val value: Int?)

val data = listOf(
    DayBarData(30, "SUN", 99),
    DayBarData(31, "MON", 99),
    DayBarData(1,  "TUE", 0),
    DayBarData(2,  "WED", 99),
    DayBarData(3,  "THU", 99),
    DayBarData(4,  "FRI", null),
    DayBarData(5,  "SAT", null)
)

val dummyDayStatuses = listOf(
    WeekDayStatus.COMPLETED,
    WeekDayStatus.COMPLETED,
    WeekDayStatus.FAILED,
    WeekDayStatus.COMPLETED,
    WeekDayStatus.COMPLETED,
    WeekDayStatus.EMPTY,
    WeekDayStatus.EMPTY
)

@Composable
fun WeeklyBarChart() {
    val validValues = data.mapNotNull { it.value }
    val maxValue = if (validValues.isEmpty()) 1 else validValues.max().coerceAtLeast(1)
    val maxIndex = data.indexOfFirst { it.value != null && it.value == validValues.max() }
    var selectedBarIndex by remember { mutableIntStateOf(-1) }
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            data.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier.weight(1f).height(38.dp),
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
                                text = "${data[maxIndex].value}",
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

        // 그래프 위 총량 표시
        Row(Modifier.fillMaxWidth()) {
            data.forEach { day ->
                Text(
                    text = if (day.value == null) "?" else "${day.value}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val colWidth = maxWidth / data.size

            // 막대 그래프
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                data.forEachIndexed { index, day ->
                    val fraction = if (day.value == null || maxValue == 0) 0f else day.value.toFloat() / maxValue
                    val animatedFraction by animateFloatAsState(
                        targetValue = if (animationStarted) fraction else 0f,
                        animationSpec = tween(durationMillis = 1000),
                        label = "barFraction$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedBarIndex = if(selectedBarIndex == index) -1 else index
                            },
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

            // 클릭 시 버블
            if (selectedBarIndex != -1) {
                val rawX = colWidth * (selectedBarIndex + 1)
                val tooltipX = rawX.coerceAtMost(maxWidth - 90.dp).coerceAtLeast(0.dp)

                Box(modifier = Modifier.offset(x = tooltipX, y = 10.dp)) {
                    BarTooltipBubble()
                }
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
            data.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${day.date}",
                        fontSize = 12.sp,
                        color = SpiritTodoTheme.color.onSurfaceColor8
                    )
                    Text(
                        text = day.dayLabel,
                        fontSize = 10.sp,
                        color = SpiritTodoTheme.color.onSurfaceColor8
                    )
                }
            }
        }
    }
}

@Composable
fun BarTooltipBubble() {
    val bubbleColor = SpiritTodoTheme.color.surfaceColor1

    Column(
        modifier = Modifier
            .drawBehind {
                val tailWidth = 8.dp.toPx()
                val tailHalfH = 6.dp.toPx()
                val r = 5.dp.toPx()
                val midY = size.height / 2f

                val path = Path().apply {
                    moveTo(0f, midY)

                    lineTo(tailWidth, midY - tailHalfH)

                    arcTo(
                        rect = Rect(tailWidth, 0f, tailWidth + r * 2, r * 2),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(size.width - r, 0f)

                    arcTo(
                        rect = Rect(size.width - r * 2, 0f, size.width, r * 2),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(size.width, size.height - r)

                    arcTo(
                        rect = Rect(size.width - r * 2, size.height - r * 2, size.width, size.height),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(tailWidth + r, size.height)

                    arcTo(
                        rect = Rect(tailWidth, size.height - r * 2, tailWidth + r * 2, size.height),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(tailWidth, midY + tailHalfH)
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
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.width(74.dp),
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
                text = "1 / 3", //dummy
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.onSurfaceColor8,
                modifier = Modifier.alignByBaseline()
            )
        }

        Row(
            modifier = Modifier.width(74.dp),
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
                text = "1 / 2", //dummy
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.onSurfaceColor8,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}