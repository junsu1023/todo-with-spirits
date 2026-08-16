package com.example.todowithspirits.feature.login

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class KakaoUserInfo(
    val kakaoUserId: String,
    val email: String?,
    val accessToken: String
)

object KakaoLoginClient {
    suspend fun login(context: Context): KakaoUserInfo {
        val token = authenticate(context)
        return fetchUserInfo(token.accessToken)
    }

    private suspend fun authenticate(context: Context): OAuthToken = suspendCancellableCoroutine { continuation ->
        val accountCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> continuation.resume(token)
                error != null -> continuation.resumeWithException(error)
                else -> continuation.resumeWithException(IllegalStateException("카카오 로그인에 실패했습니다"))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                // 사용자가 카카오톡 로그인 화면에서 직접 취소한 경우 카카오계정 로그인으로 넘어가지 않는다.
                val isUserCancelled = error is ClientError && error.reason == ClientErrorCause.Cancelled
                if (error != null && !isUserCancelled) {
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
                } else {
                    accountCallback(token, error)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
        }
    }

    private suspend fun fetchUserInfo(accessToken: String): KakaoUserInfo = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.me { user, error ->
            when {
                user != null -> continuation.resume(
                    KakaoUserInfo(
                        kakaoUserId = user.id.toString(),
                        email = user.kakaoAccount?.email,
                        accessToken = accessToken
                    )
                )
                error != null -> continuation.resumeWithException(error)
                else -> continuation.resumeWithException(IllegalStateException("카카오 사용자 정보를 가져오지 못했습니다"))
            }
        }
    }
}
