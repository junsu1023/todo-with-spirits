package com.example.todowithspirits

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.todowithspirits.component.SpiritsTodoBottomBar
import com.example.todowithspirits.component.bottomNavItems
import com.example.todowithspirits.feature.today.component.QuickAddBottomPopup
import com.example.todowithspirits.navigation.Screen
import com.example.todowithspirits.navigation.SpiritsTodoNavigation
import com.example.todowithspirits.theme.SpiritTodoTheme

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
            },
            floatingActionButton = {
                if(currentRoute == Screen.Today.route) {
                    FloatingButton(onClick = { mainViewModel.setSBottomSheetVisible(true) })
                }
            }
        ) { innerPadding ->
            SpiritsTodoNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // Rendered as a sibling of Scaffold, not from inside its floatingActionButton slot —
        // see the comment on FloatingButton for why that placement squishes the popup.
        if (currentRoute == Screen.Today.route && isBottomSheetVisible) {
            QuickAddBottomPopup(onDismiss = { mainViewModel.setSBottomSheetVisible(false) })
        }
    }
}