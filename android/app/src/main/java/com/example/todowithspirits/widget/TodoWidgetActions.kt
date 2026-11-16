package com.example.todowithspirits.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate

// 위젯은 Hilt가 주입해 주지 않으므로 EntryPoint로 저장소를 꺼내 쓴다.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TodoWidgetEntryPoint {
    fun todoWidgetRepository(): TodoWidgetRepository
}

internal fun Context.todoWidgetRepository(): TodoWidgetRepository =
    EntryPointAccessors.fromApplication(applicationContext, TodoWidgetEntryPoint::class.java).todoWidgetRepository()

// 위젯 체크박스: 일정 완료 / 완료 취소 API를 바로 호출한다.
class ChangeTaskCompletionAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdKey] ?: return
        val date = parameters[DateKey]?.let(LocalDate::parse) ?: return
        val isCompleted = parameters[IsCompletedKey] ?: return

        context.todoWidgetRepository().changeCompletion(taskId, date, isCompleted)
    }

    companion object {
        val TaskIdKey = ActionParameters.Key<Long>("taskId")
        val DateKey = ActionParameters.Key<String>("date")
        val IsCompletedKey = ActionParameters.Key<Boolean>("isCompleted")
    }
}

// 불러오기에 실패한 위젯을 눌렀을 때 다시 불러온다.
class RefreshTodoWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        context.todoWidgetRepository().requestUpdate()
    }
}
