package com.example.todowithspirits.feature.forest

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.delay

// ForestUnityActivity는 별도 프로세스(:forest)라 "닫는 중"이라는 상태를 companion object 등
// 메모리 공유 방식으로는 이 프로세스(MainActivity)에 알릴 수 없다. 대신 이 프로세스 자신이
// "Forest가 닫혔다"고 판단한 시각만 기록해두고, 그 직후 바로 재진입하려는 시도를 잠깐
// 늦춘다 — Forest를 닫을 때는 항상 finish()하는데, 그 안의 네이티브 종료 대기가 끝나기 전에
// 같은 프로세스에서 새 인스턴스가 창을 만들려 하면 포커스를 못 받아 ANR로 이어지기 때문이다.
private object ForestCloseTracker {
    @Volatile var lastCloseAtMs: Long = 0L
}

/** Unity는 별도 프로세스(:forest)의 전체 화면 Activity에서 렌더링되고, TODO 네비게이션은
 * 그 아래(별도 프로세스) 그대로 유지된다. ForestUnityActivity가 화면을 덮기 전까지
 * (진입/복귀 전환 구간) 이 화면 위에 로딩을 표시한다.
 *
 * ForestUnityActivity는 별도 프로세스라 앱 프로세스 간 메모리 공유 이벤트 버스로 "닫혔다"는
 * 신호를 보낼 수 없다. 대신 MainActivity **자신의 실제 Activity Lifecycle**을 관찰해서,
 * Forest가 화면을 덮으며 한 번 ON_PAUSE된 뒤 다시 ON_RESUME되는 순간(Forest가 사라지고 이
 * 화면으로 돌아온 순간)을 "닫힘"으로 판단한다. LocalLifecycleOwner는 Navigation-Compose
 * 안에서 NavBackStackEntry 스코프 lifecycle을 돌려주므로 쓰지 않는다 — 실제 Activity가
 * pause/resume되는 시점과 정확히 일치하지 않을 수 있기 때문이다. */
@Composable
fun ForestScreen(onReturnToToday: () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        // Forest를 막 닫은 직후(이전 인스턴스의 finish()가 아직 onDestroy()를 끝내지 못했을
        // 수 있는 구간)라면 새 startActivity를 잠깐 늦춰서, 같은(:forest) 프로세스 안에서
        // 이전 인스턴스의 종료와 새 인스턴스의 창 생성이 겹치는 걸 피한다.
        val minGapMs = 2000L
        val elapsed = System.currentTimeMillis() - ForestCloseTracker.lastCloseAtMs
        if (elapsed in 0 until minGapMs) delay(minGapMs - elapsed)
        context.startActivity(
            Intent(context, ForestUnityActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    DisposableEffect(activity) {
        val lifecycle = (activity as? LifecycleOwner)?.lifecycle
        var hasPaused = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> hasPaused = true
                Lifecycle.Event.ON_RESUME -> if (hasPaused) {
                    ForestCloseTracker.lastCloseAtMs = System.currentTimeMillis()
                    onReturnToToday()
                }
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
