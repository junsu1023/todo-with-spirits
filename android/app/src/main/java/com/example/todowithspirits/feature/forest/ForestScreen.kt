package com.example.todowithspirits.feature.forest

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.theme.SpiritTodoTheme

/** Unity renders in its own full-screen Activity; the TODO navigation stays intact underneath.
 * ForestUnityActivity가 화면을 덮기 전까지(진입/복귀 전환 구간) 이 화면 위에 로딩을 표시한다. */
@Composable
fun ForestScreen(onReturnToToday: () -> Unit) {
    val context = LocalContext.current
    var opened by rememberSaveable { mutableStateOf(false) }
    val forestLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onReturnToToday()
    }
    val enter = { forestLauncher.launch(Intent(context, ForestUnityActivity::class.java)) }

    LaunchedEffect(Unit) {
        if (!opened) {
            opened = true
            enter()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        LoadingOverlay(isLoading = true, background = SpiritTodoTheme.color.surfaceColor1)
    }
}
