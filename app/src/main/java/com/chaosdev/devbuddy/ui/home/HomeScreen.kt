package com.chaosdev.devbuddy.ui.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.navigation.BottomNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val logoutState by viewModel.logoutState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Tab setup
    val tabs = listOf("Feed", "Progress", "Challenges")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

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
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> FeedTabContent(currentUser, logoutState, viewModel)
                1 -> ProgressTabContent()
                2 -> ChallengesTabContent()
            }
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