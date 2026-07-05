package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

private data class DayBarData(val date: Int, val dayLabel: String, val value: Int?)

private enum class WeekDayStatus { COMPLETED, PARTIAL, FAILED }

private val dummyBarData = listOf(
    DayBarData(30, "SUN", 99),
    DayBarData(31, "MON", 99),
    DayBarData(1,  "TUE", 0),
    DayBarData(2,  "WED", 99),
    DayBarData(3,  "THU", 99),
    DayBarData(4,  "FRI", null),
    DayBarData(5,  "SAT", null)
)

private val dummyDayStatuses = listOf(
    WeekDayStatus.COMPLETED,
    WeekDayStatus.COMPLETED,
    WeekDayStatus.FAILED,
    WeekDayStatus.COMPLETED,
    WeekDayStatus.COMPLETED,
    WeekDayStatus.PARTIAL,
    WeekDayStatus.PARTIAL
)

@Composable
fun WeeklyReportCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(6.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = stringResource(R.string.weekly_report_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.weekly_report_subtitle),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
            }
            Image(
                painter = painterResource(R.drawable.fi_rr_sign_out),
                contentDescription = null
            )
        }

        Spacer(Modifier.height(24.dp))

        WeeklyBarChart(dummyBarData)

        Spacer(Modifier.height(16.dp))

        WeeklyStatsRow()

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = SpiritTodoTheme.colors.onSurfaceColor8)

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.weekly_record),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )

        Spacer(Modifier.height(12.dp))

        WeeklyRecordRow(dummyDayStatuses)

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.weekly_analysis),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )

        Spacer(Modifier.height(12.dp))

        WeeklyAnalysisSection(todoFraction = 0.5f, routineFraction = 0.3f, delayFraction = 0.2f)

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.weekly_top3),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )

        Spacer(Modifier.height(12.dp))

        WeeklyTop3Section()

        Spacer(Modifier.height(2.dp))

        MissedAreaRow()

        Spacer(Modifier.height(16.dp))

        WeeklyCommentCard()
    }
}

@Composable
private fun WeeklyBarChart(data: List<DayBarData>) {
    val validValues = data.mapNotNull { it.value }
    val maxValue = if (validValues.isEmpty()) 1 else validValues.max()
    val maxIndex = data.indexOfFirst { it.value == maxValue }
    val trackColor = SpiritTodoTheme.colors.onSurfaceColor3
    val crownGold = Color(0xFFFFD700)

    Column(Modifier.fillMaxWidth()) {
        // Crown badge row above max bar
        Row(Modifier.fillMaxWidth()) {
            data.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (index == maxIndex) {
                        Box(
                            modifier = Modifier
                                .background(crownGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.fi_rr_star),
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "${data[maxIndex].value}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Value labels row
        Row(Modifier.fillMaxWidth()) {
            data.forEachIndexed { index, day ->
                Text(
                    text = if (day.value == null) "?" else if (day.value == 0) "0" else "${day.value}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Bar area with tooltip overlay
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val colWidth = maxWidth / data.size

            // Bars
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                data.forEachIndexed { index, day ->
                    val fraction = if (day.value == null || maxValue == 0) 0f
                    else day.value.toFloat() / maxValue
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(fraction.coerceAtLeast(0.01f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(trackColor)
                        )
                    }
                }
            }

            // Speech bubble tooltip positioned after the max bar
            val tooltipX = colWidth * (maxIndex + 1)
            val tooltipY = 12.dp
            Box(modifier = Modifier.offset(x = tooltipX, y = tooltipY)) {
                BarTooltipBubble()
            }
        }

        Spacer(Modifier.height(6.dp))

        // Date + day labels
        Row(Modifier.fillMaxWidth()) {
            data.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${day.date}",
                        fontSize = 10.sp,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )
                    Text(
                        text = day.dayLabel,
                        fontSize = 9.sp,
                        color = SpiritTodoTheme.colors.onSurfaceColor7
                    )
                }
            }
        }
    }
}

@Composable
private fun BarTooltipBubble() {
    val bubbleColor = SpiritTodoTheme.colors.surfaceColor1
    val borderColor = SpiritTodoTheme.colors.onSurfaceColor1
    val routineColor = SpiritTodoTheme.colors.onSurfaceColor5

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
                drawPath(path, bubbleColor)
                drawPath(path, borderColor, style = Stroke(width = 1.dp.toPx()))
            }
            .padding(start = 14.dp, end = 8.dp, top = 5.dp, bottom = 5.dp)
    ) {
        Text(
            text = "To do 1/3",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = borderColor
        )
        Text(
            text = "루틴  1/2",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = routineColor
        )
    }
}

@Composable
private fun WeeklyStatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.white)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.weekly_achieved_plan),
                fontSize = 11.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "999",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
                Text(
                    text = "개",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
                Text(
                    text = " / 1000",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor7
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(SpiritTodoTheme.colors.onSurfaceColor3)
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.weekly_avg_rate),
                fontSize = 11.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "100",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )
                Text(
                    text = "%",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor1
                )
            }
        }
    }
}

@Composable
private fun WeeklyRecordRow(statuses: List<WeekDayStatus>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEachIndexed { index, status ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = when (status) {
                                WeekDayStatus.COMPLETED -> SpiritTodoTheme.colors.onSurfaceColor1
                                WeekDayStatus.PARTIAL -> SpiritTodoTheme.colors.onSurfaceColor3
                                WeekDayStatus.FAILED -> SpiritTodoTheme.colors.onSurfaceColor3
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = when (status) {
                                WeekDayStatus.COMPLETED -> SpiritTodoTheme.colors.onSurfaceColor1
                                else -> SpiritTodoTheme.colors.onSurfaceColor7
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (status) {
                        WeekDayStatus.COMPLETED -> Image(
                            painter = painterResource(R.drawable.fi_rr_color_star),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        WeekDayStatus.PARTIAL -> Image(
                            painter = painterResource(R.drawable.fi_rr_star),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        WeekDayStatus.FAILED -> Image(
                            painter = painterResource(R.drawable.fi_rr_cross),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${index + 1}일차",
                    fontSize = 9.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
            }
        }
    }
}

@Composable
private fun WeeklyAnalysisSection(
    todoFraction: Float,
    routineFraction: Float,
    delayFraction: Float
) {
    val todoColor = SpiritTodoTheme.colors.onSurfaceColor4
    val routineColor = SpiritTodoTheme.colors.onSurfaceColor5
    val delayColor = SpiritTodoTheme.colors.onSurfaceColor2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        Box(
            modifier = Modifier.weight(todoFraction).fillMaxHeight().background(todoColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(todoFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SpiritTodoTheme.colors.white
            )
        }
        Box(
            modifier = Modifier.weight(routineFraction).fillMaxHeight().background(routineColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(routineFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SpiritTodoTheme.colors.white
            )
        }
        Box(
            modifier = Modifier.weight(delayFraction).fillMaxHeight().background(delayColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(delayFraction * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SpiritTodoTheme.colors.white
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LegendDot(color = todoColor, label = "To do")
        LegendDot(color = routineColor, label = "루틴")
        LegendDot(color = delayColor, label = "미루기")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = SpiritTodoTheme.colors.mainTextColor)
    }
}

@Composable
private fun WeeklyTop3Section() {
    val colors = SpiritTodoTheme.colors
    val items = listOf(
        Triple(colors.onSurfaceColor3, "학업/커리어", "15회"),
        Triple(colors.onSurfaceColor3, "학업/커리어", "15회"),
        Triple(colors.onSurfaceColor3, "인간관계/약속", "8회"),
        Triple(colors.onSurfaceColor3, "취미", "5회")
    )

    items.forEachIndexed { index, (color, name, count) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.white)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    if (index == 0) {
                        Text(
                            text = "Top 1",
                            fontSize = 10.sp,
                            color = SpiritTodoTheme.colors.onSurfaceColor7
                        )
                    }
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )
                }
            }
            Text(
                text = count,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.mainTextColor
            )
        }
        if (index < items.lastIndex) {
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun MissedAreaRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.white)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.weekly_missed_area),
                fontSize = 11.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "😞",
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "건강",
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
            }
        }
        Text(
            text = "15회",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.colors.mainTextColor
        )
    }
}

@Composable
private fun WeeklyCommentCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.surfaceColor1, RoundedCornerShape(10.dp))
            .border(1.dp, SpiritTodoTheme.colors.onSurfaceColor1, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_color_star),
            contentDescription = null,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(R.string.weekly_comment),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.colors.onSurfaceColor1,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "다음 주는 미뤄둔 건강도 챙기며 일과 삶의 균형을 맞춰볼까요?",
                fontSize = 12.sp,
                color = SpiritTodoTheme.colors.mainTextColor,
                lineHeight = 18.sp
            )
        }
    }
}
