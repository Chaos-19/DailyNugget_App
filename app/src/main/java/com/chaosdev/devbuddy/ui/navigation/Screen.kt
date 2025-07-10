package com.chaosdev.devbuddy.ui.navigation

/**
 * Sealed class representing all the screens (routes) in the application.
 * Each object corresponds to a unique route string.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object Home : Screen("home_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")

    // You can add more screens here as your application grows
    // For example:


    // If you need to pass arguments, you can define them here
    // object Detail : Screen("detail_screen/{id}") {
    //     fun createRoute(id: String) = "detail_screen/$id"
    // }
}