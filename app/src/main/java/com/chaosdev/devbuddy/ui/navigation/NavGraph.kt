package com.chaosdev.devbuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.chaosdev.devbuddy.ui.auth.login.LoginScreen
import com.chaosdev.devbuddy.ui.auth.signup.SignUpScreen
import com.chaosdev.devbuddy.ui.home.HomeScreen
import com.chaosdev.devbuddy.ui.onboarding.OnboardingScreen // <--- Import OnboardingScreen
import com.chaosdev.devbuddy.ui.splash.SplashViewModel // <--- Import SplashViewModel

@Composable
fun NavGraph(
    // AuthRepository is usually not directly injected here, but handled by ViewModels.
    // The initial check is done in SplashViewModel.
    splashViewModel: SplashViewModel = hiltViewModel() // Get the SplashViewModel
) {
    val navController = rememberNavController()

    // Observe the determined start destination from SplashViewModel
    val startDestination by splashViewModel.startDestination.collectAsState()

    // Show a loading screen or nothing until startDestination is determined
    if (startDestination == null) {
        // You can show a blank screen, a basic loader, or keep the splash screen up longer
        // For this setup, MainActivity's setKeepOnScreenCondition handles showing the splash
        // until startDestination is non-null. So, this block is mostly a safeguard.
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!) { // Use the determined start destination
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) { // <--- Add Onboarding screen
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // Add more composable routes for other screens
    }
}
