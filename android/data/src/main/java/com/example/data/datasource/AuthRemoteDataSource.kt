package com.example.data.datasource

import com.example.data.api.AuthApi
import com.example.data.network.apiCallUnit
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi
) {
    suspend fun logout(): Result<Unit> = apiCallUnit { authApi.logout() }
}
