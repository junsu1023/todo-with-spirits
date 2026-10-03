package com.example.todowithspirits.feature.plan.viewmodel

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

// PlanListItem의 fi_rr_arrow_right(미루기) 아이콘이 거쳐가는 PlanViewModel.postponeTask를 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModelPostponeTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(repository: FakeTaskRepository) = PlanViewModel(
        getTaskCalendarUseCase = GetTaskCalendarUseCase(repository),
        deleteTasksUseCase = DeleteTasksUseCase(repository),
        completeTaskUseCase = CompleteTaskUseCase(repository),
        cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(repository),
        postponeTaskUseCase = PostponeTaskUseCase(repository),
        taskRefreshBus = TaskRefreshBus()
    )

    @Test fun postponeTaskCallsUseCaseWithGivenDates() = runTest(testDispatcher) {
        val repository = FakeTaskRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.postponeTask(
            taskId = 42L,
            originalDate = LocalDate.of(2026, 1, 1),
            newDate = LocalDate.of(2026, 1, 2)
        )
        advanceUntilIdle()

        assertEquals(
            FakeTaskRepository.PostponeCall(42L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), null),
            repository.lastPostponeCall
        )
    }

    @Test fun postponeTaskSurfacesFieldValidationMessageOnFailure() = runTest(testDispatcher) {
        val repository = FakeTaskRepository().apply {
            postponeResult = Result.failure(
                FieldValidationException(
                    mapOf("newDate" to "Weekly routines can only be postponed within the current week"),
                    "요청이 올바르지 않습니다"
                )
            )
        }
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        val messages = mutableListOf<String>()
        val collectJob = launch { viewModel.errorMsg.collect { messages.add(it) } }

        viewModel.postponeTask(taskId = 1L, originalDate = LocalDate.of(2026, 1, 1), newDate = LocalDate.of(2026, 1, 2))
        advanceUntilIdle()

        assertEquals(listOf("Weekly routines can only be postponed within the current week"), messages)
        collectJob.cancel()
    }

    @Test fun postponeTaskTogglesLoadingWhileInFlight() = runTest(testDispatcher) {
        val repository = FakeTaskRepository()
        val blocker = CompletableDeferred<Unit>()
        repository.postponeBlocker = blocker
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)

        viewModel.postponeTask(taskId = 1L, originalDate = null, newDate = LocalDate.of(2026, 1, 2))
        runCurrent()
        assertTrue(viewModel.isLoading.value)

        blocker.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }
}
