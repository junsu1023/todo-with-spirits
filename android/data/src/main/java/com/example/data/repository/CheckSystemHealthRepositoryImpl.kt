
package com.example.data.repository

import com.example.data.datasource.CheckSystemHealthRemoteDataSource
import com.example.domain.repository.CheckSystemHealthRepository
import javax.inject.Inject

class CheckSystemHealthRepositoryImpl @Inject constructor(
    private val healthCheckRemoteDataSource: CheckSystemHealthRemoteDataSource
): CheckSystemHealthRepository {
    override suspend fun checkSystemHealth(): Result<Unit> {
        return try {
            if(healthCheckRemoteDataSource.getHealth().isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Not Connected to the server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}