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
import com.example.todowithspirits.feature.record.component.DailyReportCard
import com.example.todowithspirits.feature.record.component.MonthlyReportCard
import com.example.todowithspirits.feature.record.component.RecordHeader
import com.example.todowithspirits.feature.record.component.TodayRewardCard
import com.example.todowithspirits.feature.record.component.WeeklyReportCard
import com.example.todowithspirits.theme.SpiritTodoTheme


@Composable
fun RecordScreen(navigateToAlarm: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf("일간") }
    val tabs = listOf(stringResource(R.string.daily), stringResource(R.string.weekly), stringResource(R.string.monthly))
    val weeklyTab = stringResource(R.string.weekly)
    val monthlyTab = stringResource(R.string.monthly)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.colors.white)
    ) {
        RecordHeader(navigateToAlarm = navigateToAlarm)

        val selectedIndex = tabs.indexOf(selectedTab)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tabWidth = maxWidth / tabs.size

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SpiritTodoTheme.colors.onSurfaceColor8)
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
                        SpiritTodoTheme.colors.onSurfaceColor1,
                        RoundedCornerShape(1.dp)
                    )
                    .align(Alignment.BottomStart)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) SpiritTodoTheme.colors.onSurfaceColor1
                        else SpiritTodoTheme.colors.onSurfaceColor7,
                        label = "TabTextColor$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { selectedTab = tab }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
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

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DailyTabContent() {
    Text(
        text = stringResource(R.string.daily_report),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = SpiritTodoTheme.colors.mainTextColor
    )

    Spacer(Modifier.height(14.dp))

    DailyReportCard()

    Spacer(Modifier.height(14.dp))

    TodayRewardCard()
}

@Composable
private fun WeeklyTabContent() {
    var isWeekExpanded by remember { mutableStateOf(false) }

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
            text = stringResource(R.string.week_header_format, 6, 1),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )

        Image(
            painter = painterResource(R.drawable.fi_rr_angle_small_down),
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
                .background(SpiritTodoTheme.colors.surfaceColor4)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.fi_rr_angle_small_left),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "26년 6월 1일 ~ 26년 6월 7일",
                fontSize = 14.sp,
                color = SpiritTodoTheme.colors.mainTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(R.drawable.fi_rr_angle_small_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = SpiritTodoTheme.colors.mainTextColor),
                modifier = Modifier.size(24.dp)
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    WeeklyReportCard()
}

@Composable
private fun MonthlyTabContent() {
    Text(
        text = stringResource(R.string.monthly_report),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = SpiritTodoTheme.colors.mainTextColor
    )

    Spacer(Modifier.height(14.dp))

    MonthlyReportCard()
}