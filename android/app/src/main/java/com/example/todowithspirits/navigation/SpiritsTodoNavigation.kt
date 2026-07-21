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
import com.example.todowithspirits.feature.setting.DataSettingScreen
import com.example.todowithspirits.feature.setting.DisplaySettingScreen
import com.example.todowithspirits.feature.mypage.MyPageScreen
import com.example.todowithspirits.feature.setting.AlarmSettingScreen
import com.example.todowithspirits.feature.plan.PlanDetailScreen
import com.example.todowithspirits.feature.plan.PlanScreen
import com.example.todowithspirits.feature.record.RecordScreen
import com.example.todowithspirits.feature.setting.ChangePasswordScreen
import com.example.todowithspirits.feature.setting.CustomerSupportScreen
import com.example.todowithspirits.feature.setting.EditProfileScreen
import com.example.todowithspirits.feature.splash.SplashScreen
import com.example.todowithspirits.feature.today.TodayScreen

@Composable
fun SpiritsTodoNavigation(
    navController: NavHostController,
    modifier: Modifier
) {
    val navigateToAdd: () -> Unit = { navController.navigate(Screen.Add.route) }
    val navigateToEditTask: (Long) -> Unit = { taskId ->
        navController.navigate("${Screen.Add.route}?taskId=$taskId")
    }
    val navigateToAlarm: () -> Unit = { navController.navigate(Screen.Alarm.route) }
    val navigateToAlarmSetting: () -> Unit = { navController.navigate(Screen.AlarmSetting.route) }
    val navigateToAccountSetting: () -> Unit = { navController.navigate(Screen.AccountSetting.route) }
    val navigateToDisplaySetting: () -> Unit = { navController.navigate(Screen.DisplaySetting.route) }
    val navigateToDataSetting: () -> Unit = { navController.navigate(Screen.DataSetting.route) }
    val navigateToCustomerSupport: () -> Unit = { navController.navigate(Screen.CustomerSupport.route) }
    val navigateToNicknameEdit: () -> Unit = { navController.navigate(Screen.EditProfile.route) }
    val navigateToChangePassword: () -> Unit = { navController.navigate(Screen.ChangePassword.route) }
    val navigateToDetail: (Int) -> Unit = { itemId ->
        navController.navigate("${Screen.PlanDetail.route}/$itemId")
    }
    val navigateToLogout: () -> Unit = {
        navController.navigate(Screen.Splash.route) { // 차후 로그인 화면으로 이동되도록 수정해야함
            popUpTo(0) { inclusive = true }
        }
    }
    val onBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onLoginCompleted = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Today.route) {
            TodayScreen(
                navigateToAlarm = navigateToAlarm,
                navigateToEditTask = navigateToEditTask
            )
        }

        composable(Screen.Plan.route) {
            PlanScreen(
                navigateToAdd = navigateToAdd,
                navigateToDetail = navigateToDetail,
                navigateToEditTask = navigateToEditTask,
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
                navigateToAdd = { navigateToEditTask(itemId.toLong()) }
            )
        }

        composable(Screen.Forest.route) {

        }

        composable(Screen.Record.route) {
            RecordScreen(navigateToAlarm = navigateToAlarm)
        }

        composable(
            route = "${Screen.Add.route}?taskId={taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId")?.takeIf { it != -1L }

            AddScreen(taskId = taskId, onBack = onBack)
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
                navigateToDisplaySetting = navigateToDisplaySetting,
                navigateToDataSetting = navigateToDataSetting,
                navigateToCustomerSupport = navigateToCustomerSupport,
                navigateToLogout = navigateToLogout
            )
        }

        composable(Screen.AccountSetting.route) {
            AccountSettingScreen(
                onBack = onBack,
                onNicknameClick = navigateToNicknameEdit,
                onModifyPasswordClick = navigateToChangePassword
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onBack = onBack)
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(onBack = onBack)
        }

        composable(Screen.DisplaySetting.route) {
            DisplaySettingScreen(onBack = onBack)
        }

        composable(Screen.DataSetting.route) {
            DataSettingScreen(onBack = onBack)
        }

        composable(Screen.CustomerSupport.route) {
            CustomerSupportScreen(onBack = onBack)
        }
    }
}
