package com.example.todowithspirits.feature.home.viewmodel

import com.example.todowithspirits.feature.home.state.HomeEffect
import com.example.todowithspirits.feature.home.state.HomeEvent
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

    private fun buildViewModel() = HomeViewModel()

    @Test fun todayTasksToggledFlipsExpandedState() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isTodayTasksExpanded)

        viewModel.onEvent(HomeEvent.TodayTasksToggled)
        assertTrue(viewModel.uiState.value.isTodayTasksExpanded)

        viewModel.onEvent(HomeEvent.TodayTasksToggled)
        assertFalse(viewModel.uiState.value.isTodayTasksExpanded)
    }

    @Test fun todayTaskCompletionChangedUpdatesTaskAndCounts() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val target = viewModel.uiState.value.todayTasks.first { !it.isCompleted && it.isImportant }
        val completedBefore = viewModel.uiState.value.todayCompletedCount
        val importantBefore = viewModel.uiState.value.todayImportantCount

        viewModel.onEvent(HomeEvent.TodayTaskCompletionChanged(target.id, isCompleted = true))

        val state = viewModel.uiState.value
        assertTrue(state.todayTasks.first { it.id == target.id }.isCompleted)
        assertEquals(completedBefore + 1, state.todayCompletedCount)
        assertEquals(importantBefore - 1, state.todayImportantCount)
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
        val target = viewModel.uiState.value.todayTasks.first()

        viewModel.onEvent(HomeEvent.TodayTaskClicked(target.id))
        assertEquals(target, viewModel.uiState.value.selectedTask)

        viewModel.onEvent(HomeEvent.TodayTaskDetailDismissed)
        assertEquals(null, viewModel.uiState.value.selectedTask)
    }

    @Test fun todayTaskDeleteClickedRemovesTaskAndClosesDetail() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val target = viewModel.uiState.value.todayTasks.first()
        val totalBefore = viewModel.uiState.value.todayTotalCount

        viewModel.onEvent(HomeEvent.TodayTaskClicked(target.id))
        viewModel.onEvent(HomeEvent.TodayTaskDeleteClicked(target.id))

        val state = viewModel.uiState.value
        assertTrue(state.todayTasks.none { it.id == target.id })
        assertEquals(totalBefore - 1, state.todayTotalCount)
        assertEquals(null, state.selectedTask)
    }

    @Test fun todayTaskEditClickedClosesDetailAndSendsNavigateToEditTask() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
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
