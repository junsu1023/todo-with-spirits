package com.example.domain.repository

interface CheckSystemHealthRepository {
    suspend fun checkSystemHealth(): Result<Unit>
}