package com.example.todowithspirits.feature.forest

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.feature.forest.viewmodel.ForestViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun ForestScreen(
    onReturnToToday: () -> Unit,
    forestViewModel: ForestViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.startActivity(
            Intent(context, ForestUnityActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    LaunchedEffect(forestViewModel) {
        forestViewModel.exitEvents.collect { onReturnToToday() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        LoadingOverlay(isLoading = true, background = SpiritTodoTheme.color.surfaceColor1)
    }
}
