package com.example.todowithspirits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todowithspirits.component.FloatingButton
import com.example.todowithspirits.component.QuickAddFabBottomMargin
import com.example.todowithspirits.component.QuickAddFabEndMargin
import com.example.todowithspirits.component.SpiritsTodoBottomBar
import com.example.todowithspirits.component.bottomNavItems
import com.example.todowithspirits.feature.today.component.QuickAddBottomPopup
import com.example.todowithspirits.navigation.Screen
import com.example.todowithspirits.navigation.SpiritsTodoNavigation
import com.example.todowithspirits.theme.SpiritTodoTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SpiritsTodoApp(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomSheetVisible by mainViewModel.isBottomSheetVisible.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val navToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            },
            containerColor = SpiritTodoTheme.color.surfaceColor1,
            bottomBar = {
                if(bottomNavItems.map { it.route }.contains(currentRoute)) {
                    SpiritsTodoBottomBar(
                        currentRoute = currentRoute,
                        onItemSelected = { route -> navToRoute(route) }
                    )
                }
            }
        ) { innerPadding ->
            SpiritsTodoNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // FAB와 QuickAddBottomPopup을 같은 SharedTransitionLayout/AnimatedContent 안에서 전환해,
        // FAB가 사라지고 팝업이 나타나는 게 아니라 같은 컨테이너(CONTAINER 키)와 + 아이콘(ICON 키)이
        // 원형 -> 사각형으로 애니메이션되며 이어지도록 한다.
        if (currentRoute == Screen.Today.route) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = isBottomSheetVisible,
                    transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    label = "quickAdd"
                ) { targetState ->
                    if (targetState) {
                        QuickAddBottomPopup(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent,
                            onDismiss = { mainViewModel.setSBottomSheetVisible(false) }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            FloatingButton(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@AnimatedContent,
                                onClick = { mainViewModel.setSBottomSheetVisible(true) },
                                modifier = Modifier.padding(end = QuickAddFabEndMargin, bottom = QuickAddFabBottomMargin)
                            )
                        }
                    }
                }
            }
        }
    }
}
