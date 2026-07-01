package com.example.todowithspirits.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todowithspirits.feature.add.AddScreen
import com.example.todowithspirits.feature.plan.PlanScreen
import com.example.todowithspirits.feature.today.TodayScreen

@Composable
fun SpiritsTodoNavigation(
    navController: NavHostController,
    modifier: Modifier
) {
    val navigateToAdd: () -> Unit = { navController.navigate(Screen.Add.route) }

    NavHost(
        navController = navController,
        startDestination = Screen.Today.route,
        modifier = modifier
    ) {
        composable(Screen.Today.route) {
            TodayScreen()
        }

        composable(Screen.Plan.route) {
            PlanScreen(navigateToAdd = navigateToAdd)
        }

        composable(Screen.Forest.route) {

        }

        composable(Screen.Record.route) {
        }

        composable(Screen.MyPage.route) {

        }

        composable(Screen.Add.route) {
            AddScreen()
        }
    }
}