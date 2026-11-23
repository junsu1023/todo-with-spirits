package com.example.todowithspirits.feature.setting.alarm.viewmodel

import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEffect
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEvent
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingType
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

    @Test fun alarmToggledChangesOnlyThatAlarm() = runTest(testDispatcher) {
        val viewModel = AlarmSettingViewModel()

        viewModel.onEvent(AlarmSettingEvent.AlarmToggled(AlarmSettingType.STREAK_SAVE, isOn = true))

        val state = viewModel.uiState.value
        assertTrue(state.isOn(AlarmSettingType.STREAK_SAVE))
        AlarmSettingType.entries.filter { it != AlarmSettingType.STREAK_SAVE }
            .forEach { assertFalse(state.isOn(it)) }
    }

    @Test fun backClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = AlarmSettingViewModel()

        viewModel.onEvent(AlarmSettingEvent.BackClicked)

        assertEquals(AlarmSettingEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun confirmClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = AlarmSettingViewModel()

        viewModel.onEvent(AlarmSettingEvent.ConfirmClicked)

        assertEquals(AlarmSettingEffect.NavigateBack, viewModel.effect.first())
    }
}
