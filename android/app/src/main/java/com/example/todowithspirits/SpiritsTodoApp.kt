package com.example.todowithspirits

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todowithspirits.component.SplitsTodoBottomBar
import com.example.todowithspirits.navigation.SpiritsTodoNavigation
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun SpiritsTodoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
        containerColor = SpiritTodoTheme.colors.bgColor2,
        bottomBar = {
            SplitsTodoBottomBar(
                currentRoute = currentRoute,
                onItemSelected = { route -> navToRoute(route) }
            )
        }
    ) { innerPadding ->
        SpiritsTodoNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}