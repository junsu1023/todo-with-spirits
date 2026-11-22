package com.example.todowithspirits.feature.signup.viewmodel

import android.content.ContextWrapper
import com.example.domain.exception.FieldValidationException
import com.example.domain.model.EmailAvailability
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CheckEmailAvailabilityUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.RestoreSessionUseCase
import com.example.domain.usecase.SendEmailVerificationUseCase
import com.example.domain.usecase.SignUpUseCase
import com.example.domain.usecase.UpdateUserProfileUseCase
import com.example.domain.usecase.VerifyEmailCodeUseCase
import com.example.todowithspirits.feature.signup.SignUpStep
import com.example.todowithspirits.feature.signup.state.SignUpEffect
import com.example.todowithspirits.feature.signup.state.SignUpEvent
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// 회원가입 이메일·비밀번호 입력 단계의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelEventTest {
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

    private fun buildViewModel() = SignUpViewModel(
        checkEmailAvailabilityUseCase = CheckEmailAvailabilityUseCase(authRepository),
        sendEmailVerificationUseCase = SendEmailVerificationUseCase(authRepository),
        verifyEmailCodeUseCase = VerifyEmailCodeUseCase(authRepository),
        signUpUseCase = SignUpUseCase(authRepository),
        loginUseCase = LoginUseCase(authRepository),
        updateUserProfileUseCase = UpdateUserProfileUseCase(authRepository),
        todoWidgetRepository = TodoWidgetRepository(
            context = ContextWrapper(null),
            restoreSessionUseCase = RestoreSessionUseCase(authRepository),
            getTaskCalendarUseCase = GetTaskCalendarUseCase(taskRepository),
            completeTaskUseCase = CompleteTaskUseCase(taskRepository),
            cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(taskRepository),
            taskRefreshBus = TaskRefreshBus()
        )
    )

    private fun SignUpViewModel.submit(
        email: String = "a@b.com",
        password: String = "password1",
        confirmPassword: String = password
    ) {
        onEvent(SignUpEvent.EmailChanged(email))
        onEvent(SignUpEvent.PasswordChanged(password))
        onEvent(SignUpEvent.ConfirmPasswordChanged(confirmPassword))
        onEvent(SignUpEvent.CredentialsNextClicked)
    }

    @Test fun backClickedSendsNavigateBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(SignUpEvent.BackClicked)

        assertEquals(SignUpEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun nextIsEnabledOnlyWhenAllFieldsAreFilled() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(SignUpEvent.EmailChanged("a@b.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password1"))
        assertFalse(viewModel.uiState.value.canProceedCredentials)

        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password1"))
        assertTrue(viewModel.uiState.value.canProceedCredentials)
    }

    @Test fun invalidInputShowsFieldErrorsWithoutRequest() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit(email = "invalid", password = "short", confirmPassword = "other")
        runCurrent()

        assertEquals(setOf("email", "password", "confirmPassword"), viewModel.uiState.value.fieldErrors.keys)
        assertFalse(viewModel.uiState.value.canProceedCredentials)
        assertTrue(authRepository.checkEmailRequests.isEmpty())
    }

    @Test fun editingClearsRelatedErrors() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.submit(email = "invalid", password = "short", confirmPassword = "other")

        viewModel.onEvent(SignUpEvent.PasswordChanged("password1"))
        assertEquals(setOf("email"), viewModel.uiState.value.fieldErrors.keys)

        viewModel.onEvent(SignUpEvent.EmailChanged("a@b.com"))
        assertTrue(viewModel.uiState.value.fieldErrors.isEmpty())
    }

    @Test fun registeredEmailStaysWithEmailError() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        authRepository.checkEmailResponse.complete(Result.success(EmailAvailability(null, true)))
        runCurrent()

        assertEquals(SignUpStep.CREDENTIALS, viewModel.uiState.value.step)
        assertEquals("이미 가입된 이메일입니다.", viewModel.uiState.value.fieldErrors["email"])
    }

    @Test fun serverFieldErrorIsShown() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        authRepository.checkEmailResponse.complete(
            Result.failure(FieldValidationException(mapOf("email" to "서버 검증 오류"), "오류"))
        )
        runCurrent()

        assertEquals("서버 검증 오류", viewModel.uiState.value.fieldErrors["email"])
        assertFalse(viewModel.isLoading.value)
    }

    @Test fun availableEmailMovesToVerification() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.submit()
        authRepository.checkEmailResponse.complete(Result.success(EmailAvailability(null, false)))
        authRepository.sendVerificationResponse.complete(Result.success(Unit))
        runCurrent()

        assertEquals(listOf("a@b.com"), authRepository.checkEmailRequests)
        assertEquals(SignUpStep.EMAIL_VERIFICATION, viewModel.uiState.value.step)

        // 타이머는 실제 시각을 기준으로 돌아서 runTest가 끝나도록 멈춰 준다.
        viewModel.goBackToCredentials()
    }
}
