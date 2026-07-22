package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.ReissueRequest
import com.example.data.response.ApiResponse
import com.example.data.response.ReissueResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// AccessToken 재발급 전용 API. AuthApi(Auth 필요 클라이언트)와 분리해서 NoAuth 클라이언트에 태운다.
// 재발급 호출 자체가 AuthInterceptor/TokenAuthenticator를 다시 거치면 순환 호출이 될 수 있기 때문.
interface ReissueApi {
    @POST(URLConstant.LOGIN.REISSUE)
    suspend fun reissue(@Body request: ReissueRequest): Response<ApiResponse<ReissueResponse>>
}
