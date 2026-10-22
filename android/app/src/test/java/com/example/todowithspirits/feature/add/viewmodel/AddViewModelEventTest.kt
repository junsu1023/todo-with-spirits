package com.example.todowithspirits.feature.add.viewmodel

import com.example.domain.model.AlarmOption
import com.example.domain.model.Task
import com.example.domain.usecase.CreateTodoUseCase
import com.example.domain.usecase.GetTaskUseCase
import com.example.domain.usecase.UpdateTodoUseCase
import com.example.todowithspirits.feature.add.state.AddEffect
import com.example.todowithspirits.feature.add.state.AddEvent
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
import java.time.LocalTime

// 일정 추가 화면: 루틴 없이 일정(Todo)만 만들고 고친다.
@OptIn(ExperimentalCoroutinesApi::class)
class AddViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()
    private val date = LocalDate.of(2026, 6, 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun task(id: Long, type: String = "SCHEDULE") = Task(
        category = "GROWTH", completedAt = null, createdAt = date.atStartOfDay(), endDate = date,
        endTime = LocalTime.of(9, 30), growthType = null, growthValue = 0, isAllDay = false, isCompleted = false,
        isImportant = true, isPublic = true, memo = "메모", notificationMinutes = 30, repeatDaysOfMonth = emptyList(),
        repeatDaysOfWeek = emptyList(), repeatEndDate = null, repeatType = null, startDate = null, startTime = null,
        taskId = id, taskType = type, title = "성과 보고서 제출 마감", updatedAt = date.atStartOfDay()
    )

    private fun buildViewModel(repository: FakeTaskRepository) = AddViewModel(
        createTodoUseCase = CreateTodoUseCase(repository),
        getTaskUseCase = GetTaskUseCase(repository),
        updateTodoUseCase = UpdateTodoUseCase(repository),
        taskRefreshBus = TaskRefreshBus()
    )

    @Test fun confirmIsDisabledUntilTitleIsEntered() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeTaskRepository())
        assertFalse(viewModel.uiState.value.canConfirm)

        viewModel.onEvent(AddEvent.TitleChanged("  "))
        assertFalse(viewModel.uiState.value.canConfirm)

        viewModel.onEvent(AddEvent.TitleChanged("미팅"))
        assertTrue(viewModel.uiState.value.canConfirm)
    }

    @Test fun confirmCreatesTodoAndNavigatesBack() = runTest(testDispatcher) {
        val repository = FakeTaskRepository().apply { createTodoResult = Result.success(task(1)) }
        val viewModel = buildViewModel(repository)

        viewModel.onEvent(AddEvent.TitleChanged(" 성과 보고서 제출 마감 "))
        viewModel.onEvent(AddEvent.ImportantChanged(true))
        viewModel.onEvent(AddEvent.DateChanged(date))
        viewModel.onEvent(AddEvent.TimeEnabledChanged(true))
        viewModel.onEvent(AddEvent.DueTimeChanged(LocalTime.of(17, 50)))
        viewModel.onEvent(AddEvent.AlarmOptionChanged(AlarmOption.TEN_MINUTES))
        viewModel.onEvent(AddEvent.ConfirmClicked)
        advanceUntilIdle()

        val todo = repository.lastCreatedTodo!!
        assertEquals("성과 보고서 제출 마감", todo.title)
        assertTrue(todo.isImportant)
        assertFalse(todo.isAllDay)
        assertEquals("2026-06-01T17:50:00", todo.endDateTime)
        assertEquals(AlarmOption.TEN_MINUTES, todo.notificationType)
        assertEquals(AddEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun editSendsCategoryNoneAndKeepsOtherHiddenFields() = runTest(testDispatcher) {
        val repository = FakeTaskRepository().apply {
            getTaskResult = Result.success(task(7))
            updateTodoResult = Result.success(task(7))
        }
        val viewModel = buildViewModel(repository)

        viewModel.loadTaskForEdit(7)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEditMode)
        assertEquals("성과 보고서 제출 마감", viewModel.uiState.value.title)

        viewModel.onEvent(AddEvent.TitleChanged("보고서 수정"))
        viewModel.onEvent(AddEvent.ConfirmClicked)
        advanceUntilIdle()

        val (taskId, todo) = repository.lastUpdatedTodo!!
        assertEquals(7L, taskId)
        assertEquals("보고서 수정", todo.title)
        // 카테고리는 화면에서 빠져 NONE으로 보내고, 공개 여부·메모는 불러온 그대로 보낸다.
        assertEquals("NONE", todo.category)
        assertTrue(todo.isPublic)
        assertEquals("메모", todo.memo)
    }

    @Test fun editingRoutineNavigatesBackWithoutLoading() = runTest(testDispatcher) {
        val repository = FakeTaskRepository().apply { getTaskResult = Result.success(task(9, type = "ROUTINE")) }
        val viewModel = buildViewModel(repository)

        viewModel.loadTaskForEdit(9)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editingTaskId)
        assertEquals(AddEffect.NavigateBack, viewModel.effect.first())
    }
}
