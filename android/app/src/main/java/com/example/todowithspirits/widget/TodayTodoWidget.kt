package com.example.todowithspirits.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import com.example.todowithspirits.util.KoreanMonthDayWithDayFormatter

// 4x2 '오늘 일정' 위젯: 날짜, 완료 수, 진행 바, 오늘 일정 목록
class TodayTodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.todoWidgetRepository()

        provideContent {
            val state by repository.state.collectAsState()

            LaunchedEffect(Unit) { repository.refreshIfStale() }

            TodoWidgetCard {
                when (val current = state) {
                    is TodoWidgetState.Loaded -> TodayTodoContent(current)
                    else -> TodoWidgetStatusMessage(current)
                }
            }
        }
    }
}

class TodayTodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTodoWidget()
}

@Composable
private fun ColumnScope.TodayTodoContent(state: TodoWidgetState.Loaded) {
    TodoWidgetHeader(
        title = state.today.format(KoreanMonthDayWithDayFormatter),
        completedCount = state.completedCount,
        totalCount = state.totalCount
    )

    when {
        state.totalCount == 0 -> TodoWidgetEmptyMessage(modifier = GlanceModifier.defaultWeight())

        state.isAllCompleted -> {
            Spacer(GlanceModifier.height(8.dp))
            TodoWidgetProgressBar(progress = state.progress)
            TodoWidgetMessage(
                title = "오늘 일정을 모두 끝냈어요!",
                modifier = GlanceModifier.defaultWeight()
            )
        }

        else -> {
            Spacer(GlanceModifier.height(8.dp))
            TodoWidgetProgressBar(progress = state.progress)
            Spacer(GlanceModifier.height(8.dp))
            TodoWidgetTaskList(
                tasks = state.todayTasks,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}
