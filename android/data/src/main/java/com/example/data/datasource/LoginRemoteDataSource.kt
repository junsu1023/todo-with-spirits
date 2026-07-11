package com.example.data.datasource

import com.example.data.api.LoginApi
import com.example.data.network.apiCall
import com.example.data.request.LoginRequest
import com.example.data.response.LoginResponse
import javax.inject.Inject

class LoginRemoteDataSource @Inject constructor(
    private val loginApi: LoginApi
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> =
        apiCall { loginApi.login(LoginRequest(email, password)) }
}