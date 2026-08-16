package com.example.todowithspirits.feature.login

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

data class GoogleUserInfo(
    val googleUserId: String,
    val email: String?,
    val idToken: String
)

object GoogleLoginClient {
    suspend fun login(context: Context, webClientId: String): GoogleUserInfo {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential = try {
            credentialManager.getCredential(context, request).credential
        } catch (e: GetCredentialCancellationException) {
            throw CancellationException("사용자가 구글 로그인을 취소했습니다", e)
        }

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("구글 로그인 정보를 가져오지 못했습니다")
        }

        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

        // Google 계정의 email은 GoogleIdTokenCredential에 별도로 노출되지 않아서, 백엔드로 그대로
        // 전달할 idToken(JWT)의 payload를 직접 디코딩해 sub/email 클레임을 읽는다.
        val payload = decodeIdTokenPayload(idToken)

        return GoogleUserInfo(
            googleUserId = payload.getString("sub"),
            email = payload.optString("email").takeIf { it.isNotBlank() },
            idToken = idToken
        )
    }

    private fun decodeIdTokenPayload(idToken: String): JSONObject {
        val payloadSegment = idToken.split(".").getOrNull(1)
            ?: throw IllegalStateException("잘못된 구글 idToken 형식입니다")
        val decoded = Base64.decode(payloadSegment, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        return JSONObject(String(decoded, Charsets.UTF_8))
    }
}
