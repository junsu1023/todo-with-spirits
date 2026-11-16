package com.example.todowithspirits.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.todowithspirits.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

private const val ImportantChipBackgroundAlpha = 0.16f

// 4x4 '주간 일정' 위젯: 이번 주 날짜 줄(일정 있는 날 점 표시), 중요 일정 수, 오늘 일정 목록
class WeeklyTodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.todoWidgetRepository()

        provideContent {
            val state by repository.state.collectAsState()

            LaunchedEffect(Unit) { repository.refreshIfStale() }

            TodoWidgetCard {
                when (val current = state) {
                    is TodoWidgetState.Loaded -> WeeklyTodoContent(current)
                    else -> TodoWidgetStatusMessage(current)
                }
            }
        }
    }
}

class WeeklyTodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyTodoWidget()
}

@Composable
private fun ColumnScope.WeeklyTodoContent(state: TodoWidgetState.Loaded) {
    TodoWidgetHeader(
        title = "${state.today.monthValue}월",
        completedCount = state.completedCount,
        totalCount = state.totalCount
    )

    Spacer(GlanceModifier.height(12.dp))

    WeekStrip(weekDays = state.weekDays, today = state.today)

    Spacer(GlanceModifier.height(8.dp))

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(1.dp)
            .background(WidgetColors.divider.provider())
    ) {}

    Spacer(GlanceModifier.height(12.dp))

    if (state.totalCount == 0) {
        TodoWidgetEmptyMessage(modifier = GlanceModifier.defaultWeight())
    } else {
        if (state.importantCount > 0) {
            ImportantChip(count = state.importantCount)
            Spacer(GlanceModifier.height(8.dp))
        }

        TodoWidgetTaskList(
            tasks = state.todayTasks,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

@Composable
private fun WeekStrip(
    weekDays: List<WidgetWeekDay>,
    today: LocalDate
) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        weekDays.forEach { day ->
            val isToday = day.date == today
            val dayColor = when {
                isToday -> WidgetColors.accent
                day.date.dayOfWeek == DayOfWeek.SUNDAY -> WidgetColors.sunday
                else -> WidgetColors.text
            }

            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN),
                    style = TextStyle(color = dayColor.provider(), fontSize = 12.sp)
                )

                Spacer(GlanceModifier.height(4.dp))

                Box(
                    modifier = if (isToday) {
                        GlanceModifier
                            .size(26.dp)
                            .shapeBackground(R.drawable.widget_shape_circle, WidgetColors.accent)
                    } else {
                        GlanceModifier.size(26.dp)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day.date.dayOfMonth}",
                        style = TextStyle(
                            color = (if (isToday) WidgetColors.onAccent else dayColor).provider(),
                            fontSize = 14.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }

                Spacer(GlanceModifier.height(2.dp))

                // 일정이 있는 날만 점을 찍는다. 없는 날도 같은 높이를 차지해 줄이 흔들리지 않게 한다.
                Box(
                    modifier = if (day.hasTask) {
                        GlanceModifier
                            .size(4.dp)
                            .shapeBackground(R.drawable.widget_shape_circle, WidgetColors.accent)
                    } else {
                        GlanceModifier.size(4.dp)
                    }
                ) {}
            }
        }
    }
}

@Composable
private fun ImportantChip(count: Int) {
    Row(
        modifier = GlanceModifier
            .shapeBackground(
                R.drawable.widget_shape_pill,
                WidgetColors.date.copy(alpha = ImportantChipBackgroundAlpha)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.fi_rr_color_star),
            contentDescription = null,
            modifier = GlanceModifier.size(12.dp)
        )

        Spacer(GlanceModifier.width(4.dp))

        Text(
            text = "중요 일정 ${count}건",
            style = TextStyle(
                color = WidgetColors.date.provider(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
