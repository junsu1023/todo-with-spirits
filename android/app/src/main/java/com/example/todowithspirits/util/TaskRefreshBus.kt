package com.example.todowithspirits.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/*
루틴/할 일 등 Task가 생성·변경될 때마다 이를 구독 중인 화면(Today 등)에 알려
목록을 다시 불러오게 하기 위한 앱 전역 이벤트 버스.
*/
@Singleton
class TaskRefreshBus @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> get() = _events.asSharedFlow()

    fun notifyTaskChanged() {
        _events.tryEmit(Unit)
    }
}
