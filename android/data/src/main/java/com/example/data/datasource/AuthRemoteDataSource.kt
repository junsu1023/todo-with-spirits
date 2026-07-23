package com.example.data.datasource

import com.example.data.api.AuthApi
import com.example.data.network.apiCall
import com.example.data.network.apiCallUnit
import com.example.data.request.LoginRequest
import com.example.data.request.SignUpRequest
import com.example.data.response.LoginResponse
import com.example.data.response.SignUpResponse
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> =
        apiCall { authApi.login(LoginRequest(email, password)) }

    suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResponse> =
        apiCall { authApi.signUp(SignUpRequest(email, password, nickname)) }

    suspend fun logout(): Result<Unit> = apiCallUnit { authApi.logout() }
}
