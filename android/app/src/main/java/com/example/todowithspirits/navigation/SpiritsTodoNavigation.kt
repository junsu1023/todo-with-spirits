package com.example.todowithspirits.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todowithspirits.feature.add.AddScreen
import com.example.todowithspirits.feature.alarm.AlarmScreen
import com.example.todowithspirits.feature.plan.PlanDetailScreen
import com.example.todowithspirits.feature.plan.PlanScreen
import com.example.todowithspirits.feature.today.TodayScreen

@Composable
fun SpiritsTodoNavigation(
    navController: NavHostController,
    modifier: Modifier
) {
    val navigateToAdd: () -> Unit = { navController.navigate(Screen.Add.route) }

    val navigateToAlarm: () -> Unit = { navController.navigate(Screen.Alarm.route) }

    val navigateToDetail: (Int) -> Unit = { itemId ->
        navController.navigate("${Screen.PlanDetail.route}/$itemId")
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Today.route,
        modifier = modifier
    ) {
        composable(Screen.Today.route) {
            TodayScreen(navigateToAlarm = navigateToAlarm)
        }

        composable(Screen.Plan.route) {
            PlanScreen(
                navigateToAdd = navigateToAdd,
                navigateToDetail = navigateToDetail
            )
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

        composable(Screen.Alarm.route) {
            AlarmScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "${Screen.PlanDetail.route}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable

            PlanDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
