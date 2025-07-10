package com.chaosdev.devbuddy.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.navigation.BottomNavigationBar

enum class HomeDestination(val route: String, val label: String) {
    FEED("feed", "Feed"),
    PROGRESS("progress", "Progress"),
    CHALLENGES("challenges", "Challenges")
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
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Horizontal padding for TabRow
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
                Spacer(modifier = Modifier.height(8.dp)) // Bottom spacing
            }
        }
    ) { innerPadding ->
        HomeNavHost(
            navController = tabNavController,
            startDestination = HomeDestination.FEED.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 8.dp), // Additional top padding to separate from TabRow
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
            FeedTabContent(currentUser, logoutState, viewModel)
        }
        composable(HomeDestination.PROGRESS.route) {
            ProgressTabContent()
        }
        composable(HomeDestination.CHALLENGES.route) {
            ChallengesTabContent()
        }
    }
}

@Composable
fun FeedTabContent(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    logoutState: Resource<Unit>,
    viewModel: HomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to DevBuddy!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser != null) {
            Text(text = "You are logged in as:", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${currentUser.email ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            currentUser.displayName?.let { name ->
                Text(text = "Name: $name", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Text(text = "Not logged in.", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = { viewModel.signOut() },
            enabled = logoutState !is Resource.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (logoutState is Resource.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProgressTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Progress", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Track your learning progress here.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ChallengesTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Challenges", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Explore coding challenges and tasks.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}