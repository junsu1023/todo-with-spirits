package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.ChangeCurrentSpiritRequest
import com.example.data.request.RenameSpiritRequest
import com.example.data.response.ApiResponse
import com.example.data.response.CurrencyResponse
import com.example.data.response.DecorationPurchaseResponse
import com.example.data.response.DecorationResponse
import com.example.data.response.MeetSpiritResponse
import com.example.data.response.SpiritResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// API 문서의 Spirit 구분(정령, 장식, 재화)을 한 인터페이스로 묶는다.
interface SpiritApi {
    @GET(URLConstant.SPIRIT.SPIRIT)
    suspend fun getSpirits(): Response<ApiResponse<List<SpiritResponse>>>

    @GET(URLConstant.SPIRIT.SPIRIT_CURRENT)
    suspend fun getCurrentSpirit(): Response<ApiResponse<SpiritResponse>>

    @PATCH(URLConstant.SPIRIT.SPIRIT_CURRENT)
    suspend fun changeCurrentSpirit(@Body request: ChangeCurrentSpiritRequest): Response<ApiResponse<Unit?>>

    @PATCH(URLConstant.SPIRIT.SPIRIT_NAME)
    suspend fun renameSpirit(
        @Path("spiritId") spiritId: Long,
        @Body request: RenameSpiritRequest
    ): Response<ApiResponse<Unit?>>

    @POST(URLConstant.SPIRIT.SPIRIT_MEET)
    suspend fun meetSpirit(): Response<ApiResponse<MeetSpiritResponse>>

    @GET(URLConstant.DECORATION.DECORATION)
    suspend fun getDecorations(): Response<ApiResponse<List<DecorationResponse>>>

    @POST(URLConstant.DECORATION.DECORATION_PURCHASE)
    suspend fun purchaseDecoration(
        @Path("decorationId") decorationId: Long
    ): Response<ApiResponse<DecorationPurchaseResponse>>

    @POST(URLConstant.DECORATION.DECORATION_INSTALL)
    suspend fun installDecoration(@Path("decorationId") decorationId: Long): Response<ApiResponse<Unit?>>

    @DELETE(URLConstant.DECORATION.DECORATION_INSTALL)
    suspend fun uninstallDecoration(@Path("decorationId") decorationId: Long): Response<ApiResponse<Unit?>>

    @GET(URLConstant.CURRENCY.CURRENCY)
    suspend fun getCurrencyHistory(): Response<ApiResponse<CurrencyResponse>>
}
