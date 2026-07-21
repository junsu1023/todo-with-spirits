package com.example.todowithspirits.navigation

sealed class Screen(val route: String) {
    data object Splash: Screen("splash")
    data object Today: Screen("today")
    data object Plan: Screen("plan")
    data object Forest: Screen("forest")
    data object Record: Screen("record")
    data object Add: Screen("add")
    data object PlanDetail: Screen("planDetail")
    data object Alarm: Screen("alarm")
    data object AlarmSetting: Screen("alarmSetting")
    data object MyPage: Screen("myPage")
    data object AccountSetting: Screen("accountSetting")
    data object EditProfile: Screen("editProfile")
    data object ChangePassword: Screen("changePassword")
    data object DisplaySetting: Screen("displaySetting")
    data object DataSetting: Screen("dataSetting")
    data object CustomerSupport: Screen("customerSupport")
}