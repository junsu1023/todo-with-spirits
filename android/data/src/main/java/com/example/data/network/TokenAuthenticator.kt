package com.example.data.network

import android.util.Log
import com.example.core.auth.SessionExpiredNotifier
import com.example.core.auth.TokenHolder
import com.example.core.auth.TokenStorage
import com.example.data.api.ReissueApi
import com.example.data.request.ReissueRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

private const val TAG = "TokenAuthenticator"
private const val MAX_RETRY_COUNT = 2

/*
API 응답이 401일 때 OkHttp가 자동으로 호출한다. 저장된 RefreshToken으로 AccessToken 재발급을 시도하고,
성공하면 새 토큰으로 원래 요청을 재시도한다. 재발급 자체가 실패(만료/무효화된 토큰)하면 로컬 세션을 정리하고
SessionExpiredNotifier로 알려서, 앱 어느 화면에 있든 로그인 화면으로 돌아가도록 한다.
*/
class TokenAuthenticator @Inject constructor(
    private val reissueApi: ReissueApi,
    private val tokenStorage: TokenStorage
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRY_COUNT) {
            giveUp()
            return null
        }

        // 애초에 로그인된 적이 없어 refreshToken 자체가 없다면 세션 만료 알림 없이 조용히 포기한다
        val refreshToken = tokenStorage.getRefreshToken() ?: return null

        val newTokens = runBlocking {
            runCatching {
                val reissueResponse = reissueApi.reissue(ReissueRequest(refreshToken))
                if (reissueResponse.isSuccessful) reissueResponse.body()?.detail else null
            }.onFailure {
                Log.e(TAG, "reissue failed!", it)
            }.getOrNull()
        }

        if (newTokens == null) {
            giveUp()
            return null
        }

        TokenHolder.accessToken = newTokens.accessToken
        tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun giveUp() {
        TokenHolder.accessToken = null
        tokenStorage.clear()
        SessionExpiredNotifier.notifySessionExpired()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
