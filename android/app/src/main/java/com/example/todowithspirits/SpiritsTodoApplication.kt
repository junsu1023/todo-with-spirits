package com.example.todowithspirits

import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.Process
import com.example.todowithspirits.util.TaskRefreshBus
import com.example.todowithspirits.widget.TodoWidgetRepository
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltAndroidApp
class SpiritsTodoApplication : Application() {
    @Inject
    lateinit var taskRefreshBus: TaskRefreshBus

    @Inject
    lateinit var todoWidgetRepository: TodoWidgetRepository

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 위젯은 메인 프로세스에서만 갱신한다. (숲 화면은 별도 프로세스 ":forest"로 뜬다)
        if (isMainProcess()) {
            // 앱 / 위젯 어디서든 일정이 바뀌면 홈 화면 위젯도 다시 불러온다.
            taskRefreshBus.events
                .onEach { todoWidgetRepository.requestUpdate() }
                .launchIn(applicationScope)
        }
    }

    private fun isMainProcess(): Boolean {
        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            getSystemService(ActivityManager::class.java)
                ?.runningAppProcesses
                ?.find { it.pid == Process.myPid() }
                ?.processName
        }

        return processName == null || processName == packageName
    }
}
