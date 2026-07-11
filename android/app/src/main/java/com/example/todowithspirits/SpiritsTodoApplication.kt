package com.example.todowithspirits

import android.app.Application
import android.util.Log
import com.example.core.auth.TokenHolder
import com.example.domain.usecase.LoginUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

// dummy data, Login Flow 생성되면 지울 예정
private const val TEST_EMAIL = "test@example.com"
private const val TEST_PASSWORD = "password1"

@HiltAndroidApp
class SpiritsTodoApplication : Application() {

    @Inject
    lateinit var loginUseCase: LoginUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            try {
                loginUseCase(TEST_EMAIL, TEST_PASSWORD)
                    .onFailure { Log.e("SpiritsTodoApplication", "테스트 계정 자동 로그인 실패", it) }
            } finally {
                TokenHolder.markBootstrapCompleted()
            }
        }
    }
}