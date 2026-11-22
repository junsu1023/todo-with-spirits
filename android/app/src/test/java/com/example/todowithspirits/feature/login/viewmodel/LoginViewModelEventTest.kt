package com.example.todowithspirits.feature.login.viewmodel

import android.content.ContextWrapper
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.RestoreSessionUseCase
import com.example.domain.usecase.SocialLoginUseCase
import com.example.todowithspirits.client.SocialAuthUser
import com.example.todowithspirits.feature.login.state.LoginEffect
import com.example.todowithspirits.feature.login.state.LoginEvent
import com.example.todowithspirits.testutil.FakeAuthRepository
import com.example.todowithspirits.testutil.FakeTaskRepository
import com.example.todowithspirits.util.TaskRefreshBus
import com.example.todowithspirits.widget.TodoWidgetRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

// 로그인 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelEventTest {
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

    private fun buildViewModel() = LoginViewModel(
        socialLoginUseCase = SocialLoginUseCase(authRepository),
        todoWidgetRepository = TodoWidgetRepository(
            context = ContextWrapper(null),
            restoreSessionUseCase = RestoreSessionUseCase(authRepository),
            getTaskCalendarUseCase = GetTaskCalendarUseCase(taskRepository),
            completeTaskUseCase = CompleteTaskUseCase(taskRepository),
            cancelTaskCompletionUseCase = CancelTaskCompletionUseCase(taskRepository),
            taskRefreshBus = TaskRefreshBus()
        )
    )

    private fun TestScope.collectEffects(viewModel: LoginViewModel): List<LoginEffect> {
        val effects = mutableListOf<LoginEffect>()
        backgroundScope.launch { viewModel.effect.toList(effects) }
        runCurrent()
        return effects
    }

    private val authSucceeded = LoginEvent.SocialAuthFinished(
        SocialProvider.KAKAO,
        Result.success(SocialAuthUser("id", "token", "a@b.com"))
    )
    private val session = SocialLoginSession(1, "a@b.com", "정령", "access", "refresh", "Bearer")

    @Test fun kakaoLoginClickedStartsKakaoAuth() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(LoginEvent.KakaoLoginClicked)

        assertTrue(viewModel.uiState.value.isAuthenticating)
        assertEquals(LoginEffect.StartSocialAuth(SocialProvider.KAKAO), viewModel.effect.first())
    }

    @Test fun googleLoginClickedStartsGoogleAuth() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(LoginEvent.GoogleLoginClicked)

        assertTrue(viewModel.uiState.value.isAuthenticating)
        assertEquals(LoginEffect.StartSocialAuth(SocialProvider.GOOGLE), viewModel.effect.first())
    }

    @Test fun emailLoginClickedSendsNavigateToEmailLogin() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(LoginEvent.EmailLoginClicked)

        assertEquals(LoginEffect.NavigateToEmailLogin, viewModel.effect.first())
    }

    @Test fun signUpClickedSendsNavigateToSignUp() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.onEvent(LoginEvent.SignUpClicked)

        assertEquals(LoginEffect.NavigateToSignUp, viewModel.effect.first())
    }

    @Test fun duplicateAuthWhileAuthenticatingIsIgnored() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onEvent(LoginEvent.KakaoLoginClicked)
        viewModel.onEvent(LoginEvent.KakaoLoginClicked)
        viewModel.onEvent(LoginEvent.GoogleLoginClicked)
        runCurrent()

        assertEquals(listOf(LoginEffect.StartSocialAuth(SocialProvider.KAKAO)), effects)
    }

    @Test fun authSucceededLogsInThenNavigatesHomeAndUpdatesWidget() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onEvent(LoginEvent.KakaoLoginClicked)
        viewModel.onEvent(authSucceeded)
        runCurrent()

        // 서버 응답 전에는 인증 중으로 남고 이동하지 않는다.
        assertTrue(viewModel.uiState.value.isAuthenticating)
        assertTrue(viewModel.isLoading.value)
        assertEquals(
            listOf(FakeAuthRepository.SocialLoginRequest(SocialProvider.KAKAO, "id", "token", "a@b.com")),
            authRepository.socialLoginRequests
        )
        assertFalse(LoginEffect.NavigateToHome in effects)

        authRepository.socialLoginResponse.complete(Result.success(session))
        runCurrent()

        assertFalse(viewModel.uiState.value.isAuthenticating)
        assertEquals(LoginEffect.NavigateToHome, effects.last())
        authRepository.restoreSessionRequested.await()
    }

    @Test fun serverFailureShowsErrorAndStaysOnLogin() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val effects = collectEffects(viewModel)
        val error = async { viewModel.errorMsg.first() }
        runCurrent()

        viewModel.onEvent(LoginEvent.KakaoLoginClicked)
        viewModel.onEvent(authSucceeded)
        authRepository.socialLoginResponse.complete(Result.failure(IllegalStateException("서버 오류")))
        runCurrent()

        assertEquals("서버 오류", error.await())
        assertFalse(viewModel.uiState.value.isAuthenticating)
        assertFalse(LoginEffect.NavigateToHome in effects)
    }

    @Test fun authFailedWithoutMessageShowsProviderFallback() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val error = async { viewModel.errorMsg.first() }
        runCurrent()

        viewModel.onEvent(LoginEvent.GoogleLoginClicked)
        viewModel.onEvent(LoginEvent.SocialAuthFinished(SocialProvider.GOOGLE, Result.failure(IllegalStateException())))

        assertEquals("구글 로그인에 실패했습니다", error.await())
        assertFalse(viewModel.uiState.value.isAuthenticating)
    }

    @Test fun authFailedWithMessageShowsThatMessage() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val error = async { viewModel.errorMsg.first() }
        runCurrent()

        viewModel.onEvent(LoginEvent.KakaoLoginClicked)
        viewModel.onEvent(
            LoginEvent.SocialAuthFinished(SocialProvider.KAKAO, Result.failure(IllegalStateException("SDK 오류")))
        )

        assertEquals("SDK 오류", error.await())
        assertFalse(viewModel.uiState.value.isAuthenticating)
        assertTrue(authRepository.socialLoginRequests.isEmpty())
    }

    @Test fun authCancelledShowsNoErrorAndAllowsRetry() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val effects = collectEffects(viewModel)
        val errors = mutableListOf<String>()
        backgroundScope.launch { viewModel.errorMsg.toList(errors) }
        runCurrent()

        viewModel.onEvent(LoginEvent.KakaoLoginClicked)
        viewModel.onEvent(LoginEvent.SocialAuthFinished(SocialProvider.KAKAO, Result.failure(CancellationException())))
        runCurrent()

        assertFalse(viewModel.uiState.value.isAuthenticating)
        assertEquals(emptyList<String>(), errors)

        viewModel.onEvent(LoginEvent.GoogleLoginClicked)
        advanceUntilIdle()
        runCurrent()

        assertEquals(LoginEffect.StartSocialAuth(SocialProvider.GOOGLE), effects.last())
    }
}
