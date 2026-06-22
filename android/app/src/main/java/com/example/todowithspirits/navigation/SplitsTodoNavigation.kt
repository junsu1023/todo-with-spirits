package com.example.todowithspirits.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todowithspirits.feature.record.RecordScreen
import com.example.todowithspirits.feature.today.TodayScreen

@Composable
fun SplitsTodoNavigation(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Today.route,
        modifier = modifier
    ) {
        composable(Screen.Today.route) {
            TodayScreen()
        }

        composable(Screen.Plan.route) {

        }

        composable(Screen.Forest.route) {

        }

        composable(Screen.Record.route) {
            RecordScreen()
        }

        composable(Screen.MyPage.route) {

        }
    }
}