package com.example.todowithspirits.feature.home.viewmodel

import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskSummary
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.home.state.HomeEffect
import com.example.todowithspirits.feature.home.state.HomeEvent
import com.example.todowithspirits.testutil.FakeTaskRepository
import com.example.todowithspirits.util.TaskRefreshBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

// 홈 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val today = LocalDate.now()

    private fun task(id: Long, title: String, completed: Boolean, important: Boolean, endTime: LocalTime? = null) = TaskSummary(
        completedAt = null, createdAt = today.atStartOfDay(), endTime = endTime, excludeHoliday = false,
        growthType = null, growthValue = 10, isAllDay = endTime == null, isCompleted = completed, isImportant = important,
        notificationAt = null, notificationMinutes = null, occurrenceDate = today,
        repeatDaysOfMonth = emptyList(), repeatDaysOfWeek = emptyList(), repeatEndDate = null, repeatType = null,
        taskId = id, title = title, updatedAt = today.atStartOfDay()
    )

    private val todayTasks = listOf(
        task(1, "아침 스트레칭 10분", completed = true, important = false, endTime = LocalTime.of(7, 30)),
        task(2, "팀 회의 자료 정리", completed = false, important = true, endTime = LocalTime.of(10, 0)),
        task(3, "물 2L 마시기", completed = false, important = false)
    )

    private val repository = FakeTaskRepository().apply {
        taskCalendarResult = Result.success(
            TaskCalendar(
                completedCount = 1, inCompleteCount = 2,
                totalCount = 3, items = todayTasks
            )
        )
    }

    private fun buildViewModel() = HomeViewModel(
        getTaskCalendarUseCase = GetTaskCalendarUseCase(repository),
        completeTaskUseCase = CompleteTaskUseCase(repository),
        cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(repository),
        deleteTasksUseCase = DeleteTasksUseCase(repository),
        taskRefreshBus = TaskRefreshBus()
    )

    @Test fun initLoadsTodayTasksFromCalendarApi() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(today to today, repository.lastCalendarRange)
        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L, 3L), state.todayTasks.map { it.id })
        assertEquals(LocalTime.of(10, 0), state.todayTasks[1].time)
        assertEquals(null, state.todayTasks[2].time) // 종일 일정은 시간 없이 표시
        assertEquals(3, state.todayTotalCount)
        assertEquals(1, state.todayCompletedCount)
        assertEquals(1, state.todayImportantCount)
    }

    @Test fun todayTasksToggledFlipsExpandedState() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isTodayTasksExpanded)

        viewModel.onEvent(HomeEvent.TodayTasksToggled)
        assertTrue(viewModel.uiState.value.isTodayTasksExpanded)

        viewModel.onEvent(HomeEvent.TodayTasksToggled)
        assertFalse(viewModel.uiState.value.isTodayTasksExpanded)
    }

    @Test fun todayTaskCompletionChangedCallsCompleteOrCancelApi() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.TodayTaskCompletionChanged(2, isCompleted = true))
        advanceUntilIdle()
        assertEquals(2L to today, repository.lastCompletedTask)

        viewModel.onEvent(HomeEvent.TodayTaskCompletionChanged(1, isCompleted = false))
        advanceUntilIdle()
        assertEquals(1L to today, repository.lastCanceledTask)
    }

    @Test fun settingClickedSendsNavigateToMyPageEffect() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.SettingClicked)
        advanceUntilIdle()

        assertEquals(HomeEffect.NavigateToMyPage, viewModel.effect.first())
    }

    @Test fun alarmClickedSendsNavigateToAlarmEffect() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.AlarmClicked)
        advanceUntilIdle()

        assertEquals(HomeEffect.NavigateToAlarm, viewModel.effect.first())
    }

    @Test fun bottomButtonsSendNavigateEffects() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.ForestButtonClicked)
        advanceUntilIdle()
        assertEquals(HomeEffect.NavigateToForest, viewModel.effect.first())

        viewModel.onEvent(HomeEvent.CalendarButtonClicked)
        advanceUntilIdle()
        assertEquals(HomeEffect.NavigateToCalendar, viewModel.effect.first())
    }

    @Test fun todayTaskClickedOpensDetailAndDismissCloses() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val target = viewModel.uiState.value.todayTasks.first()

        viewModel.onEvent(HomeEvent.TodayTaskClicked(target.id))
        assertEquals(target, viewModel.uiState.value.selectedTask)

        viewModel.onEvent(HomeEvent.TodayTaskDetailDismissed)
        assertEquals(null, viewModel.uiState.value.selectedTask)
    }

    @Test fun todayTaskDeleteClickedCallsDeleteApiAndClosesDetail() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val target = viewModel.uiState.value.todayTasks.first()

        viewModel.onEvent(HomeEvent.TodayTaskClicked(target.id))
        viewModel.onEvent(HomeEvent.TodayTaskDeleteClicked(target.id))
        advanceUntilIdle()

        assertEquals(listOf(target.id), repository.lastDeletedTaskIds)
        assertEquals(null, viewModel.uiState.value.selectedTask)
    }

    @Test fun todayTaskEditClickedClosesDetailAndSendsNavigateToEditTask() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val target = viewModel.uiState.value.todayTasks.first()

        viewModel.onEvent(HomeEvent.TodayTaskClicked(target.id))
        viewModel.onEvent(HomeEvent.TodayTaskEditClicked(target.id))
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.selectedTask)
        assertEquals(HomeEffect.NavigateToEditTask(target.id), viewModel.effect.first())
    }

    @Test fun spiritClickedCollapsesTodayTasksAndTogglesMenu() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.onEvent(HomeEvent.TodayTasksToggled)
        assertTrue(viewModel.uiState.value.isTodayTasksExpanded)

        viewModel.onEvent(HomeEvent.SpiritClicked)
        assertFalse(viewModel.uiState.value.isTodayTasksExpanded)
        assertTrue(viewModel.uiState.value.isSpiritMenuVisible)

        viewModel.onEvent(HomeEvent.SpiritClicked)
        assertFalse(viewModel.uiState.value.isSpiritMenuVisible)
    }

    @Test fun spiritMenuAddClickedClosesMenuAndSendsNavigateToCalendar() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.onEvent(HomeEvent.SpiritClicked)

        viewModel.onEvent(HomeEvent.SpiritMenuAddClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSpiritMenuVisible)
        assertEquals(HomeEffect.NavigateToCalendar, viewModel.effect.first())
    }
}
