package com.example.todowithspirits.feature.login

import android.content.Context
import com.example.domain.model.SocialProvider
import com.example.todowithspirits.BuildConfig

data class SocialAuthUser(
    val providerUserId: String,
    val providerAccessToken: String,
    val email: String?
)

// provider별 SDK 결과를 서버 로그인에 필요한 공통 형태로 바꾼다.
object SocialAuthClient {
    // SDK가 인증 화면을 띄우므로 context는 Activity Context여야 한다.
    suspend fun login(context: Context, provider: SocialProvider): SocialAuthUser = when (provider) {
        SocialProvider.KAKAO -> KakaoLoginClient.login(context).let { user ->
            SocialAuthUser(user.kakaoUserId, user.accessToken, user.email)
        }
        SocialProvider.GOOGLE -> GoogleLoginClient.login(context, BuildConfig.GOOGLE_WEB_CLIENT_ID).let { user ->
            SocialAuthUser(user.googleUserId, user.idToken, user.email)
        }
    }
}
