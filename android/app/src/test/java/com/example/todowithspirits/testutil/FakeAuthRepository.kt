package com.example.todowithspirits.testutil

import com.example.domain.model.EmailAvailability
import com.example.domain.model.LoginMethod
import com.example.domain.model.LoginSession
import com.example.domain.model.SignUpResult
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider
import com.example.domain.model.Subscription
import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation

// 소셜 로그인 테스트용 AuthRepository 더블.
class FakeAuthRepository : AuthRepository {
    data class SocialLoginRequest(
        val provider: SocialProvider,
        val providerUserId: String,
        val providerAccessToken: String,
        val email: String?
    )

    // 응답 시점을 테스트에서 정한다.
    val socialLoginResponse = CompletableDeferred<Result<SocialLoginSession>>()
    val socialLoginRequests = mutableListOf<SocialLoginRequest>()
    val restoreSessionRequested = CompletableDeferred<Unit>()

    override suspend fun socialLogin(
        provider: SocialProvider,
        providerUserId: String,
        providerAccessToken: String,
        email: String?
    ): Result<SocialLoginSession> {
        socialLoginRequests += SocialLoginRequest(provider, providerUserId, providerAccessToken, email)
        return socialLoginResponse.await()
    }

    // TodoWidgetRepository.requestUpdate()가 여기까지만 진행되고 실제 위젯(Glance)은 건드리지 않도록 멈춰 둔다.
    override suspend fun restoreSession(): Boolean {
        restoreSessionRequested.complete(Unit)
        awaitCancellation()
    }

    override suspend fun checkEmail(email: String): Result<EmailAvailability> = notUsed()
    override suspend fun login(email: String, password: String): Result<LoginSession> = notUsed()
    override suspend fun logout(): Result<Unit> = notUsed()
    override suspend fun withdraw(): Result<Unit> = notUsed()
    override suspend fun getUserProfile(): Result<UserProfile> = notUsed()
    override suspend fun updateUserProfile(nickname: String?, representativeSpiritId: Long?): Result<UserProfile> = notUsed()
    override suspend fun getSubscription(): Result<Subscription> = notUsed()
    override suspend fun resendEmailVerification(): Result<Unit> = notUsed()
    override suspend fun sendEmailVerification(email: String): Result<Unit> = notUsed()
    override suspend fun verifyEmailCode(email: String, code: Int): Result<Unit> = notUsed()
    override suspend fun signUp(email: String, password: String, nickname: String?): Result<SignUpResult> = notUsed()
    override fun getLoginMethod(): LoginMethod? = notUsed()

    private fun notUsed(): Nothing = error("이 테스트에서는 쓰지 않는다")
}
