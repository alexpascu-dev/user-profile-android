package com.example.myprofile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myprofile.LoginScreen
import com.example.myprofile.UserInfoScreen
import com.example.myprofile.network.auth.Jwt
import com.example.myprofile.network.auth.Session

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = remember {
        val token = Session.token()
        if (token != null && !Jwt.isExpired(token)) Screen.UserInfo.route else Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.UserInfo.route) {
            LaunchedEffect(Unit) {
                if (!Session.isLoggedIn()) {
                    navController.navigateAndClear(Screen.Login.route)
                }
            }
            UserInfoScreen(
                navController = navController,
                onLogout = {
                    Session.clear()
                    navController.navigateAndClear(Screen.Login.route)
                }
            )
        }
    }
}
fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(0)
        launchSingleTop = true
    }
}