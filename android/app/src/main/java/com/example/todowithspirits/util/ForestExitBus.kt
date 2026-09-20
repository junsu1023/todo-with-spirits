package com.example.todowithspirits.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/*
ForestUnityActivity는 별도 태스크에서 moveTaskToBack만 하고 finish()는 하지 않는다
(GameActivity.onDestroy()의 네이티브 스레드 종료 대기가 메인 스레드를 블로킹해 ANR을
일으키는 걸 피하기 위함). 그래서 Forest를 나갔다는 사실은 액티비티 결과값이 아니라
이 전역 이벤트 버스로만 ForestScreen(Compose)에 전달된다.
*/
@Singleton
class ForestExitBus @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> get() = _events.asSharedFlow()

    fun notifyExited() {
        _events.tryEmit(Unit)
    }
}
