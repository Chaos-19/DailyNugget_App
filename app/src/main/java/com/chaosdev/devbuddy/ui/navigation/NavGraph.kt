package com.chaosdev.devbuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chaosdev.devbuddy.ui.auth.login.LoginScreen
import com.chaosdev.devbuddy.ui.auth.signup.SignUpScreen
import com.chaosdev.devbuddy.ui.auth.signup.SignUpViewModel
import com.chaosdev.devbuddy.ui.home.HomeScreen
import com.chaosdev.devbuddy.ui.onboarding.OnboardingScreen
//import com.chaosdev.devbuddy.ui.splash.SplashScreen
import com.chaosdev.devbuddy.ui.splash.SplashViewModel
import com.chaosdev.devbuddy.ui.login.LoginViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    splashViewModel: SplashViewModel
) {
    val splashNavigationState = splashViewModel.navigationState.collectAsState().value
    val loginViewModel: LoginViewModel = hiltViewModel()
    val signUpViewModel: SignUpViewModel = hiltViewModel()
    val loginNavigationState = loginViewModel.navigationState.collectAsState().value
    val signUpNavigationState = signUpViewModel.navigationState.collectAsState().value

    val startDestination = when (splashNavigationState) {
        SplashViewModel.NavigationState.Login -> Screen.Login.route
        SplashViewModel.NavigationState.Home -> Screen.Home.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        /*
        composable(Screen.Splash.route) {
            SplashScreen()
        }
        */
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = loginViewModel
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                navController = navController,
                viewModel = signUpViewModel
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                navController = navController
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }

    LaunchedEffect(loginNavigationState, signUpNavigationState) {
        when {
            loginNavigationState is LoginViewModel.NavigationState.Onboarding ||
                    signUpNavigationState is SignUpViewModel.NavigationState.Onboarding -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            loginNavigationState is LoginViewModel.NavigationState.Home -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            loginNavigationState is LoginViewModel.NavigationState.GoogleSignIn -> {
                // Handled in LoginScreen
            }
            else -> Unit
        }
    }
}