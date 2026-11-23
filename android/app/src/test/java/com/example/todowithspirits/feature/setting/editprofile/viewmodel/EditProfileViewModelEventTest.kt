package com.example.todowithspirits.feature.setting.editprofile.viewmodel

import com.example.domain.model.LoginType
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.domain.usecase.UpdateUserProfileUseCase
import com.example.todowithspirits.feature.setting.editprofile.state.EditProfileEffect
import com.example.todowithspirits.feature.setting.editprofile.state.EditProfileEvent
import com.example.todowithspirits.testutil.FakeAuthRepository
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
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

// 닉네임 수정 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun profile(nickname: String) = UserProfile(
        userId = 1L,
        email = "spirit@example.com",
        emailVerificationStatus = "VERIFIED",
        loginType = LoginType.LOCAL,
        nickname = nickname,
        premium = false,
        provider = null,
        representativeSpiritId = null,
        role = UserRole.USER,
        createdAt = LocalDateTime.of(2026, 11, 23, 0, 0)
    )

    private fun buildViewModel(repository: FakeAuthRepository) = EditProfileViewModel(
        getUserProfileUseCase = GetUserProfileUseCase(repository),
        updateUserProfileUseCase = UpdateUserProfileUseCase(repository)
    )

    private fun repositoryWithProfile() = FakeAuthRepository().apply {
        userProfileResponse.complete(Result.success(profile("댕트리버")))
    }

    @Test fun currentNicknameIsFilledOnLoad() = runTest(testDispatcher) {
        val viewModel = buildViewModel(repositoryWithProfile())
        advanceUntilIdle()

        assertEquals("댕트리버", viewModel.uiState.value.nickname)
    }

    @Test fun nicknameChangedUpdatesUiState() = runTest(testDispatcher) {
        val viewModel = buildViewModel(repositoryWithProfile())
        advanceUntilIdle()

        viewModel.onEvent(EditProfileEvent.NicknameChanged("새닉네임"))

        assertEquals("새닉네임", viewModel.uiState.value.nickname)
    }

    @Test fun confirmClickedUpdatesNicknameAndNavigatesBack() = runTest(testDispatcher) {
        val repository = repositoryWithProfile()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(EditProfileEvent.NicknameChanged("새닉네임"))
        viewModel.onEvent(EditProfileEvent.ConfirmClicked)
        repository.updateProfileResponse.complete(Result.success(profile("새닉네임")))

        assertEquals(EditProfileEffect.NavigateBack, viewModel.effect.first())
        assertEquals(listOf<String?>("새닉네임"), repository.updatedNicknames)
    }

    @Test fun backClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel(repositoryWithProfile())

        viewModel.onEvent(EditProfileEvent.BackClicked)

        assertEquals(EditProfileEffect.NavigateBack, viewModel.effect.first())
    }
}
