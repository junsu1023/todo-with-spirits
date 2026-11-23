package com.example.todowithspirits.navigation

sealed class Screen(val route: String) {
    data object Splash: Screen("splash")
    data object Login: Screen("login")
    data object EmailLogin: Screen("emailLogin")
    data object SignUp: Screen("signUp")
    data object Home: Screen("home")
    data object Calendar: Screen("calendar")
    data object Search: Screen("search")
    data object Forest: Screen("forest")
    data object Bestiary: Screen("bestiary")
    data object Add: Screen("add")
    data object Alarm: Screen("alarm")
    data object AlarmSetting: Screen("alarmSetting")
    data object MainSetting: Screen("mainSetting")
    data object Withdraw: Screen("withdraw")
    data object EditProfile: Screen("editProfile")
    data object ChangePassword: Screen("changePassword")
    data object DisplaySetting: Screen("displaySetting")
    data object DataSetting: Screen("dataSetting")
}