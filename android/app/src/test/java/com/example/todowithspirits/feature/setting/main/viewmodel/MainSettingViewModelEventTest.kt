package com.example.todowithspirits.feature.setting.main.viewmodel

import com.example.domain.model.LoginMethod
import com.example.domain.model.LoginType
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.domain.usecase.GetLoginMethodUseCase
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.todowithspirits.feature.setting.main.state.MainSettingEffect
import com.example.todowithspirits.feature.setting.main.state.MainSettingEvent
import com.example.todowithspirits.feature.setting.main.state.SettingMenu
import com.example.todowithspirits.testutil.FakeAuthRepository
import kotlinx.coroutines.CompletableDeferred
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
import java.time.LocalDateTime

// 설정 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class MainSettingViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val profile = UserProfile(
        userId = 1L,
        email = "spirit@example.com",
        emailVerificationStatus = "VERIFIED",
        loginType = LoginType.LOCAL,
        nickname = "댕트리버",
        premium = false,
        provider = null,
        representativeSpiritId = null,
        role = UserRole.USER,
        createdAt = LocalDateTime.of(2026, 11, 23, 0, 0)
    )

    private fun buildViewModel(repository: FakeAuthRepository) = MainSettingViewModel(
        getUserProfileUseCase = GetUserProfileUseCase(repository),
        getLoginMethodUseCase = GetLoginMethodUseCase(repository)
    )

    @Test fun profileIsLoadedIntoUiState() = runTest(testDispatcher) {
        val repository = FakeAuthRepository().apply { userProfileResponse.complete(Result.success(profile)) }
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        assertEquals("댕트리버", viewModel.uiState.value.nickname)
        assertEquals("spirit@example.com", viewModel.uiState.value.email)
    }

    @Test fun passwordChangeIsAvailableOnlyForEmailLogin() = runTest(testDispatcher) {
        val emailUser = buildViewModel(FakeAuthRepository().apply { savedLoginMethod = LoginMethod.EMAIL })
        val socialUser = buildViewModel(FakeAuthRepository().apply { savedLoginMethod = LoginMethod.KAKAO })

        assertTrue(emailUser.uiState.value.isPasswordChangeAvailable)
        assertFalse(socialUser.uiState.value.isPasswordChangeAvailable)
    }

    @Test fun backClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeAuthRepository())

        viewModel.onEvent(MainSettingEvent.BackClicked)

        assertEquals(MainSettingEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun menuClickedNavigatesToMenu() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeAuthRepository())

        viewModel.onEvent(MainSettingEvent.MenuClicked(SettingMenu.WITHDRAW))

        assertEquals(MainSettingEffect.NavigateToMenu(SettingMenu.WITHDRAW), viewModel.effect.first())
    }

    @Test fun resumeAfterReturningRefreshesNickname() = runTest(testDispatcher) {
        val repository = FakeAuthRepository().apply { userProfileResponse.complete(Result.success(profile)) }
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        // 첫 진입 resume은 init 로딩과 겹치므로 건너뛴다.
        viewModel.onEvent(MainSettingEvent.ScreenResumed)

        // 닉네임 수정 후 돌아온 상황
        repository.userProfileResponse = CompletableDeferred(Result.success(profile.copy(nickname = "새닉네임")))
        viewModel.onEvent(MainSettingEvent.ScreenResumed)
        advanceUntilIdle()

        assertEquals("새닉네임", viewModel.uiState.value.nickname)
    }
}
