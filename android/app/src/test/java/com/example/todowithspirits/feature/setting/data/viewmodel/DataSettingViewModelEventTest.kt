package com.example.todowithspirits.feature.setting.data.viewmodel

import com.example.todowithspirits.feature.setting.data.state.DataSettingEffect
import com.example.todowithspirits.feature.setting.data.state.DataSettingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// 데이터 설정 화면의 Event → Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class DataSettingViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun backClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = DataSettingViewModel()

        viewModel.onEvent(DataSettingEvent.BackClicked)

        assertEquals(DataSettingEffect.NavigateBack, viewModel.effect.first())
    }

    @Test fun confirmClickedNavigatesBack() = runTest(testDispatcher) {
        val viewModel = DataSettingViewModel()

        viewModel.onEvent(DataSettingEvent.ConfirmClicked)

        assertEquals(DataSettingEffect.NavigateBack, viewModel.effect.first())
    }
}
