package com.example.todowithspirits.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// 겹친 요청이 모두 끝날 때까지 로딩이 유지되는지 검증한다. (BUG-10)
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelLoadingTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class TestViewModel : BaseViewModel() {
        fun request(done: CompletableDeferred<Unit>) {
            viewModelScope.launchWithLoading { done.await() }
        }

        fun failingRequest(done: CompletableDeferred<Unit>) {
            viewModelScope.launchWithLoading {
                done.await()
                throw IllegalStateException("실패")
            }
        }
    }

    @Test fun loadingStaysUntilAllOverlappingRequestsFinish() = runTest(testDispatcher) {
        val viewModel = TestViewModel()
        val first = CompletableDeferred<Unit>()
        val second = CompletableDeferred<Unit>()

        viewModel.request(first)
        viewModel.request(second)
        advanceUntilIdle()
        assertTrue(viewModel.isLoading.value)

        first.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.isLoading.value)

        second.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test fun failedRequestDoesNotEndOtherRequestLoading() = runTest(testDispatcher) {
        val viewModel = TestViewModel()
        val failing = CompletableDeferred<Unit>()
        val pending = CompletableDeferred<Unit>()

        viewModel.failingRequest(failing)
        viewModel.request(pending)
        advanceUntilIdle()

        failing.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.isLoading.value)

        pending.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }
}
