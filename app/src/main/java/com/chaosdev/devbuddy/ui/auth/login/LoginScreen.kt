package com.chaosdev.devbuddy.ui.auth.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.login.LoginViewModel
import com.chaosdev.devbuddy.ui.navigation.Screen
import com.google.android.gms.auth.api.identity.BeginSignInResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current as Activity

    val uiState by viewModel.uiState.collectAsState()
    val googleSignInFlow by viewModel.googleSignInFlow.collectAsState()
    val navigationState by viewModel.navigationState.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                viewModel.completeGoogleSignIn(credential)
            } catch (e: Exception) {
                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
                viewModel.resetGoogleSignInFlow()
            }
        } else {
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            viewModel.resetGoogleSignInFlow()
        }
    }

    LaunchedEffect(navigationState) {
        when (val state = navigationState) {
            is LoginViewModel.NavigationState.Onboarding -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginViewModel.NavigationState.Home -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginViewModel.NavigationState.GoogleSignIn -> {
                state.beginSignInResult.pendingIntent?.intentSender?.let { intentSender ->
                    val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                }
                viewModel.resetGoogleSignInFlow()
            }
            null -> Unit
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginViewModel.UiState.Error -> {
                val currentUiState = uiState
                if (currentUiState is LoginViewModel.UiState.Error) {
                    Toast.makeText(context, currentUiState.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUiState()
                }
                viewModel.resetUiState()
            }
            is LoginViewModel.UiState.Loading -> {
                // Show loading indicator
            }
            is LoginViewModel.UiState.Idle -> {
                // Initial state
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.loginWithEmail(email, password) },
            enabled = uiState !is LoginViewModel.UiState.Loading && googleSignInFlow !is Resource.Loading<BeginSignInResult>,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is LoginViewModel.UiState.Loading && googleSignInFlow !is Resource.Loading<BeginSignInResult>) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login with Email")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Screen.SignUp.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Don't have an account? Sign Up")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Text("OR", modifier = Modifier.padding(vertical = 8.dp))
        Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.beginGoogleSignIn() },
            enabled = uiState !is LoginViewModel.UiState.Loading && googleSignInFlow !is Resource.Loading<BeginSignInResult>,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            if (googleSignInFlow is Resource.Loading<BeginSignInResult>) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary)
            } else {
                Text("Sign in with Google")
            }
        }
    }
}