package com.example.core.auth

import kotlinx.coroutines.CompletableDeferred

object TokenHolder {
    @Volatile
    var accessToken: String? = null

    /*
    앱 기동 시 자동 로그인이 끝나기 전에 인증 API가 먼저 나가는 것을 막기 위한 신호.
    로그인 성공/실패와 무관하게 시도가 끝났을 때 한 번만 완료된다.
    */
    private val bootstrapCompleted = CompletableDeferred<Unit>()

    suspend fun awaitBootstrap() = bootstrapCompleted.await()

    fun markBootstrapCompleted() {
        if (!bootstrapCompleted.isCompleted) bootstrapCompleted.complete(Unit)
    }
}
