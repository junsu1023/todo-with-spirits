package com.example.todowithspirits.navigation

sealed class Screen(val route: String) {
    data object Today: Screen("today")
    data object Plan: Screen("plan")
    data object Forest: Screen("forest")
    data object Record: Screen("record")
    data object MyPage: Screen("myPage")
}