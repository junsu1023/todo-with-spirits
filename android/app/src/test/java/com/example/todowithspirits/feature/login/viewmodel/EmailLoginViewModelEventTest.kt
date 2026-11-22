package com.example.todowithspirits.feature.login.viewmodel

import android.content.ContextWrapper
import com.example.domain.model.LoginSession
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.RestoreSessionUseCase
import com.example.todowithspirits.feature.login.state.EmailLoginEffect
import com.example.todowithspirits.feature.login.state.EmailLoginEvent
import com.example.todowithspirits.testutil.FakeAuthRepository
import com.example.todowithspirits.testutil.FakeTaskRepository
import com.example.todowithspirits.util.TaskRefreshBus
import com.example.todowithspirits.widget.TodoWidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// 이메일 로그인 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class EmailLoginViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = FakeAuthRepository()
    private val taskRepository = FakeTaskRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = EmailLoginViewModel(
        loginUseCase = LoginUseCase(authRepository),
        todoWidgetRepository = TodoWidgetRepository(
            context = ContextWrapper(null),
            restoreSessionUseCase = RestoreSessionUseCase(authRepository),
            getTaskCalendarUseCase = GetTaskCalendarUseCase(taskRepository),
            completeTaskUseCase = CompleteTaskUseCase(taskRepository),
            cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(taskRepository),
            taskRefreshBus = TaskRefreshBus()
        )
    )

    private fun EmailLoginViewModel.submit() {
        onEvent(EmailLoginEvent.EmailChanged("a@b.com"))
        onEvent(EmailLoginEvent.PasswordChanged("password"))
        onEvent(EmailLoginEvent.LoginClicked)
    }

    @Test fun backClickedSendsNavigateBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(EmailLoginEvent.BackClicked)

        assertEquals(EmailLoginEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun findPasswordClickedSendsNavigateToFindPassword() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(EmailLoginEvent.FindPasswordClicked)

        assertEquals(EmailLoginEffect.NavigateToFindPassword, viewModel.effect.first())
    }

    @Test fun loginIsEnabledOnlyWhenBothFieldsAreFilled() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(EmailLoginEvent.EmailChanged("a@b.com"))
        assertFalse(viewModel.uiState.value.canLogin)

        viewModel.onEvent(EmailLoginEvent.PasswordChanged(" "))
        assertFalse(viewModel.uiState.value.canLogin)

        viewModel.onEvent(EmailLoginEvent.PasswordChanged("password"))
        assertTrue(viewModel.uiState.value.canLogin)
    }

    @Test fun loginSucceededNavigatesHomeAndUpdatesWidget() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        runCurrent()

        assertTrue(viewModel.isLoading.value)
        assertEquals(listOf("a@b.com" to "password"), authRepository.loginRequests)

        authRepository.loginResponse.complete(
            Result.success(LoginSession(1, "a@b.com", "정령", "access", "refresh", "Bearer"))
        )

        assertEquals(EmailLoginEffect.NavigateToHome, viewModel.effect.first())
        runCurrent()
        assertFalse(viewModel.isLoading.value)
        authRepository.restoreSessionRequested.await()
    }

    @Test fun loginFailedShowsMessageUntilDismissed() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        authRepository.loginResponse.complete(Result.failure(IllegalStateException("서버 오류")))
        runCurrent()

        assertEquals("서버 오류", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.isLoading.value)

        viewModel.onEvent(EmailLoginEvent.ErrorDialogDismissed)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test fun loginFailedWithoutMessageShowsFallback() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        authRepository.loginResponse.complete(Result.failure(IllegalStateException()))
        runCurrent()

        assertEquals("로그인에 실패했습니다", viewModel.uiState.value.errorMessage)
    }

    @Test fun unexpectedExceptionAlsoShowsDialog() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        authRepository.loginResponse.completeExceptionally(IllegalStateException("예상 밖 오류"))
        runCurrent()

        assertEquals("예상 밖 오류", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.isLoading.value)
    }
}
