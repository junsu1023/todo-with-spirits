package com.example.data.network

import com.example.core.auth.TokenHolder
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 앱 기동 시 자동 로그인이 끝나기 전에 요청이 나가면 토큰 없이 401을 받게 되므로, 로그인 시도가 끝날 때까지 이 요청을 보내는 스레드에서 대기
        runBlocking { TokenHolder.awaitBootstrap() }

        val request = chain.request()
        val accessToken = TokenHolder.accessToken

        val authorizedRequest = if (accessToken != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }

        return chain.proceed(authorizedRequest)
    }
}
