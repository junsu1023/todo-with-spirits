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
import com.example.todowithspirits.feature.mypage.AccountSettingScreen
import com.example.todowithspirits.feature.setting.DisplaySettingScreen
import com.example.todowithspirits.feature.mypage.MyPageScreen
import com.example.todowithspirits.feature.setting.AlarmSettingScreen
import com.example.todowithspirits.feature.plan.PlanDetailScreen
import com.example.todowithspirits.feature.plan.PlanScreen
import com.example.todowithspirits.feature.record.RecordScreen
import com.example.todowithspirits.feature.today.TodayScreen

@Composable
fun SpiritsTodoNavigation(
    navController: NavHostController,
    modifier: Modifier
) {
    val navigateToAdd: () -> Unit = { navController.navigate(Screen.Add.route) }
    val navigateToAlarm: () -> Unit = { navController.navigate(Screen.Alarm.route) }
    val navigateToAlarmSetting: () -> Unit = { navController.navigate(Screen.AlarmSetting.route) }
    val navigateToAccountSetting: () -> Unit = { navController.navigate(Screen.AccountSetting.route) }
    val navigateToDisplaySetting: () -> Unit = { navController.navigate(Screen.DisplaySetting.route) }
    val navigateToDetail: (Int) -> Unit = { itemId ->
        navController.navigate("${Screen.PlanDetail.route}/$itemId")
    }
    val onBack: () -> Unit = { navController.popBackStack() }

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
                navigateToDetail = navigateToDetail,
                navigateToAlarm = navigateToAlarm
            )
        }

        composable(
            route = "${Screen.PlanDetail.route}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable

            PlanDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                navigateToAdd = navigateToAdd
            )
        }

        composable(Screen.Forest.route) {

        }

        composable(Screen.Record.route) {
            RecordScreen(navigateToAlarm = navigateToAlarm)
        }

        composable(Screen.Add.route) {
            AddScreen(onBack = onBack)
        }

        composable(Screen.Alarm.route) {
            AlarmScreen(
                onBack = onBack,
                onSettingClick = navigateToAlarmSetting
            )
        }

        composable(Screen.AlarmSetting.route) {
            AlarmSettingScreen(onBack = onBack)
        }

        composable(Screen.MyPage.route) {
            MyPageScreen(
                navigateToAccountSetting = navigateToAccountSetting,
                navigateToAlarmSetting = navigateToAlarmSetting,
                navigateToDisplaySetting = navigateToDisplaySetting
            )
        }

        composable(Screen.AccountSetting.route) {
            AccountSettingScreen(onBack = onBack)
        }

        composable(Screen.DisplaySetting.route) {
            DisplaySettingScreen(onBack = onBack)
        }
    }
}
