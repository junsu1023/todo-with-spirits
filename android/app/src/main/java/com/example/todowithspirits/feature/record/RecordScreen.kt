package com.example.todowithspirits.feature.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.record.component.DailyReportCard
import com.example.todowithspirits.feature.record.component.MonthlyReportCard
import com.example.todowithspirits.feature.record.component.TodayRewardCard
import com.example.todowithspirits.feature.record.component.WeeklyReportCard
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RecordScreen(navigateToAlarm: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf("일간") }
    val tabs = listOf(stringResource(R.string.daily), stringResource(R.string.weekly), stringResource(R.string.monthly))
    val weeklyTab = stringResource(R.string.weekly)
    val monthlyTab = stringResource(R.string.monthly)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        TitleHeader(
            title = stringResource(R.string.record_title),
            rightIconRes = R.drawable.todo_alarm,
            onRightIconClick = navigateToAlarm,
            isAlarm = true
        )

        val selectedIndex = tabs.indexOf(selectedTab)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tabWidth = maxWidth / tabs.size

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(SpiritTodoTheme.color.surfaceColor16)
                    .align(Alignment.BottomStart)
            )

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex + (tabWidth - 109.dp) / 2,
                animationSpec = spring(stiffness = 500f),
                label = "TabIndicator"
            )

            Box(
                modifier = Modifier
                    .width(109.dp)
                    .height(2.dp)
                    .offset(x = indicatorOffset)
                    .background(
                        SpiritTodoTheme.color.mainTextAndStroke,
                        RoundedCornerShape(1.dp)
                    )
                    .align(Alignment.BottomStart)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    val textColor by animateColorAsState(
                        targetValue = if(isSelected) SpiritTodoTheme.color.mainTextAndStroke
                        else SpiritTodoTheme.color.onSurfaceColor8,
                        label = "TabTextColor$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { selectedTab = tab }
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 16.sp,
                            fontWeight = if(isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            when (selectedTab) {
                weeklyTab -> WeeklyTabContent()
                monthlyTab -> MonthlyTabContent()
                else -> DailyTabContent()
            }

            Spacer(Modifier.height(5.dp))
        }
    }
}

@Composable
private fun DailyTabContent() {
    Text(
        text = stringResource(R.string.daily_report),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = SpiritTodoTheme.color.todoTextMain
    )

    Spacer(Modifier.height(14.dp))

    DailyReportCard()

    Spacer(Modifier.height(14.dp))

    TodayRewardCard()
}

@Composable
private fun WeeklyTabContent() {
    var isWeekExpanded by remember { mutableStateOf(false) }
    var weekStart by remember { mutableStateOf(LocalDate.now().with(DayOfWeek.MONDAY)) }
    val weekEnd = weekStart.plusDays(6)
    val weekOfMonth = (weekStart.dayOfMonth - 1) / 7 + 1
    val weekDateFormatter = remember { DateTimeFormatter.ofPattern("yy년 M월 d일", Locale.KOREAN) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { isWeekExpanded = !isWeekExpanded }
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.week_header_format, weekStart.monthValue, weekOfMonth),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Image(
            painter = painterResource(R.drawable.todo_arrow1),
            contentDescription = null,
            modifier = Modifier
                .rotate(if(isWeekExpanded) 180f else 0f)
        )
    }

    AnimatedVisibility(
        visible = isWeekExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(6.dp))
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.fi_rr_angle_small_left),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { weekStart = weekStart.minusWeeks(1) }
            )

            Text(
                text = "${weekStart.format(weekDateFormatter)} ~ ${weekEnd.format(weekDateFormatter)}",
                fontSize = 16.sp,
                color = SpiritTodoTheme.color.todoTextMain,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(R.drawable.fi_rr_angle_small_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = SpiritTodoTheme.color.todoTextMain),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { weekStart = weekStart.plusWeeks(1) }
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    WeeklyReportCard()
}

@Composable
private fun MonthlyTabContent() {
    Text(
        text = stringResource(R.string.monthly_report, 7),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = SpiritTodoTheme.color.todoTextMain
    )

    Spacer(Modifier.height(14.dp))

    MonthlyReportCard()
}