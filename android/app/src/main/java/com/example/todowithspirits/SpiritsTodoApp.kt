package com.example.todowithspirits

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todowithspirits.navigation.Screen
import com.example.todowithspirits.navigation.SpiritsTodoNavigation
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

// 홈 하단 버튼으로 들어가는 화면. 뒤로가기 시 홈으로 돌아간다.
private val homeButtonRoutes = setOf(Screen.Forest.route, Screen.Calendar.route)

// mainViewModel은 앱 시작 시 시스템 상태 확인(init)을 위해 생성만 한다.
@Suppress("UNUSED_PARAMETER")
@Composable
fun SpiritsTodoApp(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val activity = LocalActivity.current
    val context = LocalContext.current
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
            Screen.Home.route -> confirmExit()
            in homeButtonRoutes -> {
                val movedToHome = navController.popBackStack(Screen.Home.route, inclusive = false)
                if (!movedToHome) {
                    navController.navigate(Screen.Home.route) {
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

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            )
        },
        containerColor = SpiritTodoTheme.color.surfaceColor1
    ) { innerPadding ->
        // 홈은 언덕 배경이 시스템 내비게이션 바 뒤까지 이어지도록 bottom padding을 적용하지 않는다.
        val bottomPadding = if (currentRoute == Screen.Home.route) 0.dp else innerPadding.calculateBottomPadding()

        SpiritsTodoNavigation(
            navController = navController,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = bottomPadding
            )
        )
    }
}
