package com.chaosdev.devbuddy.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.home.feed.FeedScreen
import com.chaosdev.devbuddy.ui.home.saved.SavedScreen
import com.chaosdev.devbuddy.ui.home.trending.TrendingScreen
import com.chaosdev.devbuddy.ui.navigation.BottomNavigationBar

enum class HomeDestination(val route: String, val label: String) {
    FEED("feed", "For You"),
    TRENDING("trending", "Trending"),
    SAVED("saved", "Saved")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val logoutState by viewModel.logoutState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Nested NavController for tab navigation
    val tabNavController = rememberNavController()
    var selectedDestination by rememberSaveable { mutableIntStateOf(HomeDestination.FEED.ordinal) }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is Resource.Success -> {
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("login_screen") {
                    popUpTo("home_screen") { inclusive = true }
                }
                viewModel.resetLogoutState()
            }

            is Resource.Error -> {
                Toast.makeText(context, logoutState.message, Toast.LENGTH_LONG).show()
                viewModel.resetLogoutState()
            }

            is Resource.Loading -> {
                // Show loading indicator
            }

            is Resource.Idle -> {
                // Initial state
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController as NavHostController) },
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Home")
                    }
                )
                TabRow(
                    selectedTabIndex = selectedDestination,
                    modifier = Modifier
                        .fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    HomeDestination.values().forEachIndexed { index, destination ->
                        Tab(
                            selected = selectedDestination == index,
                            onClick = {
                                tabNavController.navigate(destination.route) {
                                    // Pop up to the start destination to avoid building up a large stack
                                    popUpTo(tabNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple instances of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting
                                    restoreState = true
                                }
                                selectedDestination = index
                            },
                            text = {
                                Text(
                                    text = destination.label,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

            }
        }
    ) { innerPadding ->
        HomeNavHost(
            navController = tabNavController,
            startDestination = HomeDestination.FEED.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            currentUser = currentUser,
            logoutState = logoutState,
            viewModel = viewModel
        )
    }
}

@Composable
fun HomeNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    logoutState: Resource<Unit>,
    viewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(HomeDestination.FEED.route) {
            FeedScreen(currentUser, logoutState, viewModel)
        }
        composable(HomeDestination.TRENDING.route) {
            TrendingScreen()
        }
        composable(HomeDestination.SAVED.route) {
            SavedScreen()
        }
    }
}
