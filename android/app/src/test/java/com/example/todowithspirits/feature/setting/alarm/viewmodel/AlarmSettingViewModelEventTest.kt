package com.example.todowithspirits.feature.setting.alarm.viewmodel

import com.example.domain.model.NotificationSetting
import com.example.domain.usecase.GetNotificationSettingUseCase
import com.example.domain.usecase.UpdateNotificationSettingUseCase
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEffect
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEvent
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingType
import com.example.todowithspirits.testutil.FakeNotificationSettingRepository
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

// 알림 설정 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmSettingViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val savedSetting = NotificationSetting(
        reminderEnabled = true,
        postponedTaskEnabled = false,
        routineReminderEnabled = true,
        streakSaveEnabled = false,
        nightPushEnabled = true
    )

    private fun buildViewModel(repository: FakeNotificationSettingRepository) = AlarmSettingViewModel(
        getNotificationSettingUseCase = GetNotificationSettingUseCase(repository),
        updateNotificationSettingUseCase = UpdateNotificationSettingUseCase(repository)
    )

    @Test fun savedSettingIsLoadedIntoUiState() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeNotificationSettingRepository(savedSetting))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOn(AlarmSettingType.ACTIVE_REMIND))
        assertFalse(state.isOn(AlarmSettingType.POSTPONE_PLAN))
        assertTrue(state.isOn(AlarmSettingType.ROUTINE_GUIDE))
        assertFalse(state.isOn(AlarmSettingType.STREAK_SAVE))
        assertTrue(state.isOn(AlarmSettingType.NIGHT_PUSH))
    }

    @Test fun alarmToggledSendsOnlyThatAlarm() = runTest(testDispatcher) {
        val repository = FakeNotificationSettingRepository(savedSetting)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AlarmSettingEvent.AlarmToggled(AlarmSettingType.STREAK_SAVE, isOn = true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOn(AlarmSettingType.STREAK_SAVE))
        assertEquals(
            listOf(FakeNotificationSettingRepository.UpdateRequest(null, null, null, true, null)),
            repository.updateRequests
        )
    }

    @Test fun failedUpdateRevertsToggle() = runTest(testDispatcher) {
        val repository = FakeNotificationSettingRepository(
            savedSetting,
            updateResult = Result.failure(IllegalStateException("network"))
        )
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AlarmSettingEvent.AlarmToggled(AlarmSettingType.NIGHT_PUSH, isOn = false))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOn(AlarmSettingType.NIGHT_PUSH))
    }

    @Test fun backClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeNotificationSettingRepository(savedSetting))

        viewModel.onEvent(AlarmSettingEvent.BackClicked)

        assertEquals(AlarmSettingEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun confirmClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeNotificationSettingRepository(savedSetting))

        viewModel.onEvent(AlarmSettingEvent.ConfirmClicked)

        assertEquals(AlarmSettingEffect.NavigateBack, viewModel.effect.first())
    }
}
