package com.example.myprofile.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object UserInfo : Screen("userInfo")
}
