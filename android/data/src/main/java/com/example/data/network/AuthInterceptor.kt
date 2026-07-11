package com.example.data.network

import com.example.core.auth.TokenHolder
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
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
