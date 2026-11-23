package com.example.todowithspirits.feature.setting.display.viewmodel

import com.example.domain.model.AppLanguage
import com.example.domain.model.DisplaySetting
import com.example.domain.usecase.GetDisplaySettingUseCase
import com.example.domain.usecase.UpdateDisplaySettingUseCase
import com.example.todowithspirits.feature.setting.display.state.DisplaySettingEffect
import com.example.todowithspirits.feature.setting.display.state.DisplaySettingEvent
import com.example.todowithspirits.testutil.FakeDisplaySettingRepository
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

// 디스플레이 설정 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class DisplaySettingViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val savedSetting = DisplaySetting(darkMode = true, ddayDisplayEnabled = false, language = AppLanguage.ENGLISH)

    private fun buildViewModel(repository: FakeDisplaySettingRepository) = DisplaySettingViewModel(
        getDisplaySettingUseCase = GetDisplaySettingUseCase(repository),
        updateDisplaySettingUseCase = UpdateDisplaySettingUseCase(repository)
    )

    @Test fun savedSettingIsLoadedIntoUiState() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeDisplaySettingRepository(savedSetting))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isDarkMode)
        assertFalse(state.isShowPlanDday)
        assertEquals("English", state.languageOption)
    }

    @Test fun planDdayToggledUpdatesStateAndSendsOnlyThatValue() = runTest(testDispatcher) {
        val repository = FakeDisplaySettingRepository(savedSetting)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(DisplaySettingEvent.PlanDdayToggled(true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isShowPlanDday)
        assertEquals(listOf(FakeDisplaySettingRepository.UpdateRequest(null, true, null)), repository.updateRequests)
    }

    @Test fun languageSelectedSendsAppLanguage() = runTest(testDispatcher) {
        val repository = FakeDisplaySettingRepository(savedSetting)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(DisplaySettingEvent.LanguageSelected("한국어"))
        advanceUntilIdle()

        assertEquals("한국어", viewModel.uiState.value.languageOption)
        assertEquals(AppLanguage.KOREAN, repository.updateRequests.single().language)
    }

    @Test fun failedUpdateRevertsValues() = runTest(testDispatcher) {
        val repository = FakeDisplaySettingRepository(savedSetting, updateFailure = IllegalStateException("network"))
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(DisplaySettingEvent.DarkModeToggled(false))
        viewModel.onEvent(DisplaySettingEvent.LanguageSelected("한국어"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDarkMode)
        assertEquals("English", viewModel.uiState.value.languageOption)
    }

    @Test fun confirmClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeDisplaySettingRepository(savedSetting))

        viewModel.onEvent(DisplaySettingEvent.ConfirmClicked)

        assertEquals(DisplaySettingEffect.NavigateBack, viewModel.effect.first())
    }
}
