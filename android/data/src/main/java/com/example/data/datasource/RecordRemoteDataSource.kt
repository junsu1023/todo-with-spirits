package com.example.data.datasource

import com.example.data.api.RecordApi
import com.example.data.network.apiCall
import com.example.data.response.DailyRecordResponse
import javax.inject.Inject

class RecordRemoteDataSource @Inject constructor(
    private val recordApi: RecordApi
) {
    suspend fun getTodayRecord(): Result<DailyRecordResponse> = apiCall { recordApi.getTodayRecord() }
}
