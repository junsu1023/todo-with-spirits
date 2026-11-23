package com.example.todowithspirits.feature.alarm.viewmodel

import com.example.domain.model.NotificationCategory
import com.example.domain.model.NotificationItem
import com.example.domain.model.NotificationPage
import com.example.domain.usecase.GetNotificationsUseCase
import com.example.domain.usecase.MarkAllNotificationsReadUseCase
import com.example.todowithspirits.feature.alarm.state.AlarmEffect
import com.example.todowithspirits.feature.alarm.state.AlarmEvent
import com.example.todowithspirits.testutil.FakeNotificationRepository
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

// 알림 화면의 Event → UiState / Effect 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun notification(id: Long, read: Boolean) =
        NotificationItem(id, NotificationCategory.SYSTEM, "알림 $id", LocalDateTime.of(2026, 11, 23, 0, 0), read)

    private fun buildViewModel(repository: FakeNotificationRepository) = AlarmViewModel(
        getNotificationsUseCase = GetNotificationsUseCase(repository),
        markAllNotificationsReadUseCase = MarkAllNotificationsReadUseCase(repository)
    )

    private val mixedPage = NotificationPage(
        items = listOf(notification(1, read = false), notification(2, read = true), notification(3, read = false)),
        nextCursor = null,
        hasNext = false
    )

    @Test fun notificationsAreSplitIntoNewAndPast() = runTest(testDispatcher) {
        val viewModel = buildViewModel(FakeNotificationRepository(mixedPage))
        advanceUntilIdle()

        assertEquals(listOf(1L, 3L), viewModel.uiState.value.newAlarms.map { it.id })
        assertEquals(listOf(2L), viewModel.uiState.value.pastAlarms.map { it.id })
    }

    @Test fun backClickedMarksAllReadAndNavigatesBack() = runTest(testDispatcher) {
        val repository = FakeNotificationRepository(mixedPage)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AlarmEvent.BackClicked)

        assertEquals(AlarmEffect.NavigateBack, viewModel.effect.first())
        advanceUntilIdle()
        assertEquals(1, repository.markAllReadCount)
    }

    @Test fun settingClickedMarksAllReadAndNavigatesToSetting() = runTest(testDispatcher) {
        val repository = FakeNotificationRepository(mixedPage)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AlarmEvent.SettingClicked)

        assertEquals(AlarmEffect.NavigateToSetting, viewModel.effect.first())
        advanceUntilIdle()
        assertEquals(1, repository.markAllReadCount)
    }

    @Test fun leavingWithoutNewAlarmsSkipsMarkRead() = runTest(testDispatcher) {
        val repository = FakeNotificationRepository(
            NotificationPage(listOf(notification(1, read = true)), null, false)
        )
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AlarmEvent.BackClicked)

        assertEquals(AlarmEffect.NavigateBack, viewModel.effect.first())
        advanceUntilIdle()
        assertEquals(0, repository.markAllReadCount)
    }
}
