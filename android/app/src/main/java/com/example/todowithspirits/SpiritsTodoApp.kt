package com.example.todowithspirits

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.todowithspirits.util.ToastUtil

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SpiritsTodoApp(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomSheetVisible by mainViewModel.isBottomSheetVisible.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val activity = LocalActivity.current
    val context = LocalContext.current
    val bottomBarRoutes = remember { bottomNavItems.map { it.route }.toSet() }
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }

    val confirmExit: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastBackPressedAt <= 2000L) {
            activity?.finish()
        } else {
            lastBackPressedAt = now
            ToastUtil.show(context, "뒤로 버튼을 한 번 더 누르면 종료됩니다")
        }
    }

    BackHandler(enabled = currentRoute != null) {
        when (currentRoute) {
            Screen.Today.route -> confirmExit()
            in bottomBarRoutes -> {
                val movedToToday = navController.popBackStack(Screen.Today.route, inclusive = false)
                if (!movedToToday) {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            else -> {
                if (!navController.popBackStack()) confirmExit()
            }
        }
    }

    val navToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Screen.Today.route) {
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
