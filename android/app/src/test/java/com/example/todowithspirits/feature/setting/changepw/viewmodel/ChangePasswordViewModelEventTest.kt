package com.example.todowithspirits.feature.setting.changepw.viewmodel

import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordEffect
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// 비밀번호 변경 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun ChangePasswordViewModel.fill(current: String, new: String, confirm: String) {
        onEvent(ChangePasswordEvent.CurrentPasswordChanged(current))
        onEvent(ChangePasswordEvent.NewPasswordChanged(new))
        onEvent(ChangePasswordEvent.ConfirmPasswordChanged(confirm))
    }

    @Test fun canProceedOnlyWhenAllFieldsFilled() = runTest(testDispatcher) {
        val viewModel = ChangePasswordViewModel()
        viewModel.fill("old1234a", "new1234a", "")
        assertFalse(viewModel.uiState.value.canProceed)

        viewModel.onEvent(ChangePasswordEvent.ConfirmPasswordChanged("new1234a"))
        assertTrue(viewModel.uiState.value.canProceed)
    }

    @Test fun invalidNewPasswordShowsFormatError() = runTest(testDispatcher) {
        val viewModel = ChangePasswordViewModel()
        viewModel.fill("old1234a", "abcdefgh", "abcdefgh")

        viewModel.onEvent(ChangePasswordEvent.NextClicked)

        assertNotNull(viewModel.uiState.value.fieldErrors["newPassword"])
    }

    @Test fun mismatchedConfirmShowsErrorAndEditingClearsIt() = runTest(testDispatcher) {
        val viewModel = ChangePasswordViewModel()
        viewModel.fill("old1234a", "new1234a", "new1234b")

        viewModel.onEvent(ChangePasswordEvent.NextClicked)
        assertNotNull(viewModel.uiState.value.fieldErrors["confirmPassword"])

        viewModel.onEvent(ChangePasswordEvent.ConfirmPasswordChanged("new1234a"))
        assertTrue(viewModel.uiState.value.fieldErrors.isEmpty())
    }

    @Test fun backClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = ChangePasswordViewModel()

        viewModel.onEvent(ChangePasswordEvent.BackClicked)

        assertEquals(ChangePasswordEffect.NavigateBack, viewModel.effect.first())
    }
}
