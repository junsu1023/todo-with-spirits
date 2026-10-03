package com.example.todowithspirits.feature.today.viewmodel

import com.example.domain.exception.FieldValidationException
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.PostponeTaskUseCase
import com.example.todowithspirits.testutil.FakeTaskRepository
import com.example.todowithspirits.util.TaskRefreshBus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

// TodoDetailBottomSheet의 "미루기" 버튼이 거쳐가는 TodayViewModel.postponeTodo를 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelPostponeTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(repository: FakeTaskRepository) = TodayViewModel(
        getTaskCalendarUseCase = GetTaskCalendarUseCase(repository),
        completeTaskUseCase = CompleteTaskUseCase(repository),
        cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(repository),
        deleteTasksUseCase = DeleteTasksUseCase(repository),
        postponeTaskUseCase = PostponeTaskUseCase(repository),
        taskRefreshBus = TaskRefreshBus()
    )

    @Test fun postponeTodoCallsUseCaseWithGivenDatesAndRunsOnSuccess() = runTest(testDispatcher) {
        val repository = FakeTaskRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        var onSuccessCalled = false
        viewModel.postponeTodo(
            taskId = 217L,
            originalDate = LocalDate.of(2026, 1, 1),
            newDate = LocalDate.of(2026, 1, 2),
            onSuccess = { onSuccessCalled = true }
        )
        advanceUntilIdle()

        assertEquals(
            FakeTaskRepository.PostponeCall(217L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), null),
            repository.lastPostponeCall
        )
        assertTrue(onSuccessCalled)
    }

    @Test fun postponeTodoSurfacesFieldValidationMessageOnFailure() = runTest(testDispatcher) {
        val repository = FakeTaskRepository().apply {
            postponeResult = Result.failure(
                FieldValidationException(
                    mapOf("newDate" to "Daily routines cannot change date, only time"),
                    "요청이 올바르지 않습니다"
                )
            )
        }
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        val messages = mutableListOf<String>()
        val collectJob = launch { viewModel.errorMsg.collect { messages.add(it) } }

        viewModel.postponeTodo(taskId = 1L, newDate = LocalDate.of(2026, 1, 2))
        advanceUntilIdle()

        assertEquals(listOf("Daily routines cannot change date, only time"), messages)
        collectJob.cancel()
    }

    @Test fun postponeTodoTogglesLoadingWhileInFlight() = runTest(testDispatcher) {
        val repository = FakeTaskRepository()
        val blocker = CompletableDeferred<Unit>()
        repository.postponeBlocker = blocker
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)

        viewModel.postponeTodo(taskId = 1L, newDate = LocalDate.of(2026, 1, 2))
        runCurrent()
        assertTrue(viewModel.isLoading.value)

        blocker.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }
}
