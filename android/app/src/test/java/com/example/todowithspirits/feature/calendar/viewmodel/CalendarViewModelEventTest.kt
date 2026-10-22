package com.example.todowithspirits.feature.calendar.viewmodel

import com.example.domain.model.PlanSortOption
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskSummary
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.calendar.state.CalendarEffect
import com.example.todowithspirits.feature.calendar.state.CalendarEvent
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

// 캘린더 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 10, 13)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun task(id: Long, title: String, completed: Boolean = false, important: Boolean = false) = TaskSummary(
        category = "NONE", completedAt = null, createdAt = today.atStartOfDay(), endDate = today, endTime = null,
        growthType = null, growthValue = 0, isAllDay = true, isCompleted = completed, isImportant = important,
        isPublic = false, memo = null, notificationAt = null, notificationMinutes = null, occurrenceDate = today,
        taskId = id, taskType = "SCHEDULE", title = title, updatedAt = today.atStartOfDay()
    )

    private fun buildViewModel(vararg tasks: TaskSummary): CalendarViewModel {
        val repository = FakeTaskRepository().apply {
            taskCalendarResult = Result.success(
                TaskCalendar(
                    completedCount = 0, completedRoutineCount = 0, completedScheduleCount = 0, inCompleteCount = 0,
                    routineCount = 0, scheduleCount = 0, totalCount = tasks.size, items = tasks.toList()
                )
            )
        }
        return CalendarViewModel(
            getTaskCalendarUseCase = GetTaskCalendarUseCase(repository),
            deleteTasksUseCase = DeleteTasksUseCase(repository),
            completeTaskUseCase = CompleteTaskUseCase(repository),
            cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(repository),
            taskRefreshBus = TaskRefreshBus()
        )
    }

    @Test fun taskClickedExpandsOnlyOneCardAtATime() = runTest(testDispatcher) {
        val viewModel = buildViewModel(task(1, "회의"), task(2, "운동"))
        advanceUntilIdle()

        viewModel.onEvent(CalendarEvent.TaskClicked(1))
        assertEquals(1, viewModel.uiState.value.expandedTaskId)

        viewModel.onEvent(CalendarEvent.TaskClicked(2))
        assertEquals(2, viewModel.uiState.value.expandedTaskId)

        viewModel.onEvent(CalendarEvent.TaskClicked(2))
        assertNull(viewModel.uiState.value.expandedTaskId)
    }

    @Test fun hideCompletedFiltersDoneTasksAndDropsCompleteSort() = runTest(testDispatcher) {
        val viewModel = buildViewModel(task(1, "회의", completed = true), task(2, "운동"))
        advanceUntilIdle()
        viewModel.onEvent(CalendarEvent.SortOptionSelected(PlanSortOption.COMPLETE))

        viewModel.onEvent(CalendarEvent.HideCompletedToggled)

        val state = viewModel.uiState.value
        assertTrue(state.isHidden)
        assertEquals(listOf(2), state.visiblePlans.map { it.id })
        assertEquals(PlanSortOption.DEADLINE, state.sortOption)
        assertFalse(PlanSortOption.COMPLETE in state.sortOptions)
    }

    @Test fun importantSortPutsImportantTasksFirst() = runTest(testDispatcher) {
        val viewModel = buildViewModel(task(1, "가"), task(2, "나", important = true))
        advanceUntilIdle()

        viewModel.onEvent(CalendarEvent.SortOptionSelected(PlanSortOption.IMPORTANT))

        assertEquals(listOf(2, 1), viewModel.uiState.value.visiblePlans.map { it.id })
    }

    @Test fun editAndAddSendNavigateEffects() = runTest(testDispatcher) {
        val viewModel = buildViewModel(task(7, "회의"))
        advanceUntilIdle()

        viewModel.onEvent(CalendarEvent.TaskEditClicked(7))
        advanceUntilIdle()
        assertEquals(CalendarEffect.NavigateToEditTask(7L), viewModel.effect.first())

        viewModel.onEvent(CalendarEvent.AddClicked)
        advanceUntilIdle()
        assertEquals(CalendarEffect.NavigateToAdd, viewModel.effect.first())
    }

    @Test fun dateSelectedCollapsesExpandedCard() = runTest(testDispatcher) {
        val viewModel = buildViewModel(task(1, "회의"))
        advanceUntilIdle()
        viewModel.onEvent(CalendarEvent.TaskClicked(1))

        viewModel.onEvent(CalendarEvent.DateSelected(today.plusDays(1)))

        assertEquals(today.plusDays(1), viewModel.uiState.value.selectedDate)
        assertNull(viewModel.uiState.value.expandedTaskId)
    }
}
