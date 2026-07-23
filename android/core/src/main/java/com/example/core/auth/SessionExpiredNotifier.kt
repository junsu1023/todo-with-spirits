package com.example.core.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/*
RefreshToken마저 만료/무효화되어 세션을 완전히 복구할 수 없을 때(TokenAuthenticator) 발생시키는 이벤트.
어느 화면에 있든 앱 전역에서 이 이벤트를 구독해 로그인 화면으로 돌려보내는 데 사용한다.
*/
object SessionExpiredNotifier {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> get() = _events.asSharedFlow()

    fun notifySessionExpired() {
        _events.tryEmit(Unit)
    }
}
