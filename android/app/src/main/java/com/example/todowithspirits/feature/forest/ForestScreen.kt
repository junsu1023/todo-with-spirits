package com.example.todowithspirits.feature.forest

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.theme.SpiritTodoTheme

/** Unity는 별도 프로세스(:forest)의 전체 화면 Activity에서 렌더링되고, TODO 네비게이션은
 * 그 아래(별도 프로세스) 그대로 유지된다. ForestUnityActivity가 화면을 덮기 전까지
 * (진입/복귀 전환 구간) 이 화면 위에 로딩을 표시한다.
 *
 * ForestUnityActivity는 별도 프로세스라 앱 프로세스 간 메모리 공유 이벤트 버스로 "닫혔다"는
 * 신호를 보낼 수 없다. 대신 MainActivity **자신의 실제 Activity Lifecycle**을 관찰해서,
 * Forest가 화면을 덮으며 한 번 ON_PAUSE된 뒤 다시 ON_RESUME되는 순간(Forest가 사라지고 이
 * 화면으로 돌아온 순간)을 "닫힘"으로 판단한다. LocalLifecycleOwner는 Navigation-Compose
 * 안에서 NavBackStackEntry 스코프 lifecycle을 돌려주므로 쓰지 않는다 — 실제 Activity가
 * moveTaskToBack으로 pause/resume되는 시점과 정확히 일치하지 않을 수 있기 때문이다. */
@Composable
fun ForestScreen(onReturnToToday: () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    // 바텀바 탭 재진입 시 같은 Compose 세션 안에서 빠르게 여러 번 눌려도 startActivity가
    // 겹쳐 호출되지 않도록 하는 로컬(같은 프로세스) 디바운스. 시간 기준이라 무한 로딩으로
    // 이어지지 않는다(rememberSaveable 불리언 1회성 가드와 달리 재진입을 영구히 막지 않음).
    var lastLaunchAtMs by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        if (now - lastLaunchAtMs > 2000) {
            lastLaunchAtMs = now
            context.startActivity(
                Intent(context, ForestUnityActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    DisposableEffect(activity) {
        val lifecycle = (activity as? LifecycleOwner)?.lifecycle
        var hasPaused = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> hasPaused = true
                Lifecycle.Event.ON_RESUME -> if (hasPaused) onReturnToToday()
                else -> {}
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        LoadingOverlay(isLoading = true, background = SpiritTodoTheme.color.surfaceColor1)
    }
}
