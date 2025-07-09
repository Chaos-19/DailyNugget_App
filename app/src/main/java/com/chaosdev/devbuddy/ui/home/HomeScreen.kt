package com.chaosdev.devbuddy.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.navigation.BottomNavigationBar
import com.chaosdev.devbuddy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val logoutState by viewModel.logoutState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is Resource.Success -> {
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
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
        bottomBar = { BottomNavigationBar(navController as NavHostController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome to DevBuddy!", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (currentUser != null) {
                Text(text = "You are logged in as:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Email: ${currentUser?.email ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                currentUser?.displayName?.let { name ->
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
}