package com.chaosdev.devbuddy.ui.navigation;

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.chaosdev.devbuddy.ui.auth.login.LoginScreen
import chaosdev.devbuddy.ui.auth.signup.SignUpScreen
import com.chaosdev.devbuddy.ui.home.HomeScreen

@Composable
fun NavGraph(
    auth: FirebaseAuth = FirebaseAuth.getInstance() // Direct instance for initial check
) {
    val navController = rememberNavController()
    val startDestination = if (auth.currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // Add more composable routes for other screens
    }
}