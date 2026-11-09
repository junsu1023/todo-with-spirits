package com.example.data.datasource

import com.example.data.api.SpiritApi
import com.example.data.network.apiCall
import com.example.data.network.apiCallUnit
import com.example.data.request.ChangeCurrentSpiritRequest
import com.example.data.request.RenameSpiritRequest
import com.example.data.response.CurrencyResponse
import com.example.data.response.DecorationPurchaseResponse
import com.example.data.response.DecorationResponse
import com.example.data.response.MeetSpiritResponse
import com.example.data.response.SpiritResponse
import javax.inject.Inject

class SpiritRemoteDataSource @Inject constructor(
    private val spiritApi: SpiritApi
) {
    suspend fun getSpirits(): Result<List<SpiritResponse>> = apiCall { spiritApi.getSpirits() }

    suspend fun getCurrentSpirit(): Result<SpiritResponse> = apiCall { spiritApi.getCurrentSpirit() }

    suspend fun changeCurrentSpirit(spiritId: Long): Result<Unit> =
        apiCallUnit { spiritApi.changeCurrentSpirit(ChangeCurrentSpiritRequest(spiritId)) }

    suspend fun renameSpirit(spiritId: Long, spiritName: String): Result<Unit> =
        apiCallUnit { spiritApi.renameSpirit(spiritId, RenameSpiritRequest(spiritName)) }

    suspend fun meetSpirit(): Result<MeetSpiritResponse> = apiCall { spiritApi.meetSpirit() }

    suspend fun getDecorations(): Result<List<DecorationResponse>> = apiCall { spiritApi.getDecorations() }

    suspend fun purchaseDecoration(decorationId: Long): Result<DecorationPurchaseResponse> =
        apiCall { spiritApi.purchaseDecoration(decorationId) }

    suspend fun installDecoration(decorationId: Long): Result<Unit> =
        apiCallUnit { spiritApi.installDecoration(decorationId) }

    suspend fun uninstallDecoration(decorationId: Long): Result<Unit> =
        apiCallUnit { spiritApi.uninstallDecoration(decorationId) }

    suspend fun getCurrencyHistory(): Result<CurrencyResponse> = apiCall { spiritApi.getCurrencyHistory() }
}
