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
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todowithspirits.navigation.Screen
import com.example.todowithspirits.navigation.SpiritsTodoNavigation
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

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
            ToastUtil.show(context, context.getString(R.string.one_more_back))
        }
    }

    BackHandler(enabled = currentRoute != null) {
        when (currentRoute) {
            Screen.Home.route -> confirmExit()
            in setOf(Screen.Forest.route, Screen.Calendar.route) -> {
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
        SpiritsTodoNavigation(
            navController = navController,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        )
    }
}
