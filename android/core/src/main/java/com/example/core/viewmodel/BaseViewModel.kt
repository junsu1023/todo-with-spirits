package com.example.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/*
모든 ViewModel의 기반이 될 BaseViewModel
공통 기능(로딩, 에러 메시지)/코루틴 실행 편의 기능 제공
 */
open class BaseViewModel: ViewModel() {
    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    protected val _errorMsg = MutableSharedFlow<String>()
    val errorMsg: SharedFlow<String> get() = _errorMsg.asSharedFlow()

    // 로딩 상태 관리 및 에러 핸들링
    fun CoroutineScope.launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        handleError: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) = this.launch(
        context = context,
        start = start
    ) {
        try {
            _isLoading.update { true }
            block()
        } catch (e: Exception) {
            if(e is CancellationException) {
                throw e
            }

            if(handleError) {
                emitErrorMsg(e.localizedMessage ?: "알 수 없는 에러 발생")
            }
        } finally {
            _isLoading.update { false }
        }
    }

    fun emitErrorMsg(msg: String) {
        viewModelScope.launch {
            _errorMsg.emit(msg)
        }
    }
}