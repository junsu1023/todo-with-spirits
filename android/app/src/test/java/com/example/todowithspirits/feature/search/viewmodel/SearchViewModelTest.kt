package com.example.todowithspirits.feature.search.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskSummary
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.search.state.SearchEvent
import com.example.todowithspirits.testutil.FakeTaskRepository
import com.example.todowithspirits.feature.search.model.RecentSearchStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

// 일정 검색 화면: 넘어온 검색어로 바로 검색하고, 날짜별로 묶는다.
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val recentSearchStore = RecentSearchStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun task(id: Long, title: String, date: LocalDate) = TaskSummary(
        category = "NONE", completedAt = null, createdAt = date.atStartOfDay(), endDate = date, endTime = null,
        growthType = null, growthValue = 0, isAllDay = true, isCompleted = false, isImportant = false,
        isPublic = false, memo = null, notificationAt = null, notificationMinutes = null, occurrenceDate = date,
        taskId = id, taskType = "SCHEDULE", title = title, updatedAt = date.atStartOfDay()
    )

    private fun buildViewModel(query: String, vararg tasks: TaskSummary): SearchViewModel {
        val repository = FakeTaskRepository().apply {
            taskCalendarResult = Result.success(
                TaskCalendar(
                    completedCount = 0, completedRoutineCount = 0, completedScheduleCount = 0, inCompleteCount = 0,
                    routineCount = 0, scheduleCount = 0, totalCount = tasks.size, items = tasks.toList()
                )
            )
        }
        return SearchViewModel(
            savedStateHandle = SavedStateHandle(mapOf(SEARCH_QUERY_ARG to query)),
            getTaskCalendarUseCase = GetTaskCalendarUseCase(repository),
            recentSearchStore = recentSearchStore
        )
    }

    @Test fun searchesInitialQueryAndGroupsResultsByDateInOrder() = runTest(testDispatcher) {
        val may29 = LocalDate.of(2026, 5, 29)
        val jun1 = LocalDate.of(2026, 6, 1)
        val viewModel = buildViewModel(
            "미팅",
            task(1, "A법인 방문 미팅", jun1),
            task(2, "운동", may29),
            task(3, "팀 미팅", may29),
            task(4, "미팅 준비", may29)
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("미팅", state.input)
        assertEquals(listOf(may29, jun1), state.dateGroups.map { it.date })
        assertEquals(setOf(3, 4), state.dateGroups.first().items.map { it.id }.toSet())
        assertTrue(recentSearchStore.searches.value.isEmpty())
    }

    @Test fun submittingNewQueryReSearchesAndRecordsRecentSearch() = runTest(testDispatcher) {
        val date = LocalDate.of(2026, 6, 1)
        val viewModel = buildViewModel("미팅", task(1, "미팅", date), task(2, "마감", date))
        advanceUntilIdle()

        viewModel.onEvent(SearchEvent.InputChanged("마감"))
        viewModel.onEvent(SearchEvent.Submitted)
        advanceUntilIdle()

        assertEquals(listOf(2), viewModel.uiState.value.results.map { it.id })
        assertEquals(listOf("마감"), recentSearchStore.searches.value)
    }
}
