package com.example.todowithspirits

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todowithspirits.component.FloatingButton
import com.example.todowithspirits.component.SpiritsTodoBottomBar
import com.example.todowithspirits.component.bottomNavItems
import com.example.todowithspirits.navigation.Screen
import com.example.todowithspirits.navigation.SpiritsTodoNavigation
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SpiritsTodoApp(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomSheetVisible by mainViewModel.isBottomSheetVisible.collectAsStateWithLifecycle()

    val navToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
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
                FloatingButton(
                    isBottomSheetVisible = isBottomSheetVisible,
                    setBottomSheetVisible = { visible -> mainViewModel.setSBottomSheetVisible(visible) }
                )
            }
        }
    ) { innerPadding ->
        SpiritsTodoNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}