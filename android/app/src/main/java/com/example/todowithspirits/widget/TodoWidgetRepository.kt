package com.example.todowithspirits.widget

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.example.core.auth.TokenHolder
import com.example.core.tag.TAG
import com.example.domain.model.TaskSummary
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.RestoreSessionUseCase
import com.example.todowithspirits.util.TaskRefreshBus
import com.example.todowithspirits.util.ToastUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/*
홈 화면 위젯이 보여줄 이번 주 / 오늘 일정을 들고 있는 저장소.
위젯은 앱 화면(Splash)을 거치지 않고 프로세스가 뜰 수 있어서, 일정을 부를 때마다 직접 세션을 복원한다.
*/
@Singleton
class TodoWidgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val cancelTaskCompletionUseCase: CancelTaskCompletionUseCase,
    private val taskRefreshBus: TaskRefreshBus
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    private var lastRefreshedAt = 0L

    private val _state = MutableStateFlow<TodoWidgetState>(TodoWidgetState.Loading)
    val state: StateFlow<TodoWidgetState> get() = _state.asStateFlow()

    // 앱에서 일정이 바뀌거나 로그인 상태가 바뀌었을 때 호출한다.
    fun requestUpdate() {
        scope.launch {
            refresh()
            updateWidgets()
        }
    }

    // 위젯 세션이 새로 열릴 때 호출한다. 방금 불러온 일정이면 다시 부르지 않는다.
    suspend fun refreshIfStale() {
        val loaded = _state.value as? TodoWidgetState.Loaded
        val isFresh = loaded != null &&
            loaded.today == LocalDate.now() &&
            SystemClock.elapsedRealtime() - lastRefreshedAt < STALE_MILLIS

        if (!isFresh) refresh()
    }

    // 앱 화면 없이 프로세스가 떴다면 AuthInterceptor가 부트스트랩 완료를 기다리므로, 복원 후 완료를 알린다.
    suspend fun restoreSession(): Boolean {
        val isRestored = runCatching { restoreSessionUseCase() }.getOrDefault(false)
        TokenHolder.markBootstrapCompleted()
        return isRestored
    }

    suspend fun changeCompletion(taskId: Long, date: LocalDate, isCompleted: Boolean) {
        if (!restoreSession()) {
            _state.value = TodoWidgetState.LoggedOut
            updateWidgets()
            return
        }

        // 서버 응답을 기다리지 않고 체크 표시부터 바꾼다.
        updateTask(taskId) { it.copy(isCompleted = isCompleted) }
        updateWidgets()

        val result = if (isCompleted) {
            completeTaskUseCase(taskId, date).map { }
        } else {
            cancelTaskCompletionUseCase(taskId, date)
        }

        result
            .onSuccess { taskRefreshBus.notifyTaskChanged() }
            .onFailure {
                Log.e(TAG, "widget changeCompletion failed!", it)
                updateTask(taskId) { task -> task.copy(isCompleted = !isCompleted) }
                updateWidgets()

                val fallback = if (isCompleted) "완료 처리에 실패했습니다" else "완료 취소에 실패했습니다"
                withContext(Dispatchers.Main) { ToastUtil.show(context, it.localizedMessage ?: fallback) }
            }
    }

    private suspend fun refresh() {
        refreshMutex.withLock { refreshLocked() }
    }

    private suspend fun refreshLocked() {
        if (!restoreSession()) {
            _state.value = TodoWidgetState.LoggedOut
            return
        }

        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val week = (0L..6L).map { monday.plusDays(it) }

        getTaskCalendarUseCase(week.first(), week.last())
            .onSuccess { calendar ->
                val datesWithTask = calendar.items.map { it.occurrenceDate }.toSet()

                _state.value = TodoWidgetState.Loaded(
                    today = today,
                    weekDays = week.map { WidgetWeekDay(date = it, hasTask = it in datesWithTask) },
                    todayTasks = calendar.items
                        .filter { it.occurrenceDate == today }
                        .map { it.toWidgetTask() }
                )
                lastRefreshedAt = SystemClock.elapsedRealtime()
            }
            .onFailure {
                Log.e(TAG, "widget refresh failed!", it)
                if (_state.value !is TodoWidgetState.Loaded) _state.value = TodoWidgetState.Error
            }
    }

    private fun updateTask(taskId: Long, transform: (WidgetTask) -> WidgetTask) {
        _state.update { state ->
            if (state !is TodoWidgetState.Loaded) return@update state
            state.copy(todayTasks = state.todayTasks.map { if (it.id == taskId) transform(it) else it })
        }
    }

    private suspend fun updateWidgets() {
        TodayTodoWidget().updateAll(context)
        WeeklyTodoWidget().updateAll(context)
    }

    companion object {
        private const val STALE_MILLIS = 60_000L
    }
}

// 종일 일정과 루틴은 시간 없이 표시한다. (홈 '오늘 일정'과 같은 규칙)
private fun TaskSummary.toWidgetTask(): WidgetTask = WidgetTask(
    id = taskId,
    title = title,
    isCompleted = isCompleted,
    isImportant = isImportant,
    date = occurrenceDate,
    time = endTime.takeUnless { isAllDay }
)
