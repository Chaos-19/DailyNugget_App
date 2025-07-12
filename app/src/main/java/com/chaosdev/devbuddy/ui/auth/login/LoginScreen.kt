package com.chaosdev.devbuddy.ui.auth.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chaosdev.devbuddy.R
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.login.LoginViewModel
import com.chaosdev.devbuddy.ui.navigation.Screen
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity

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
                val credential =
                    Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                viewModel.completeGoogleSignIn(credential)
            } catch (e: Exception) {
                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG)
                    .show()
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
                state.beginSignInResult.pendingIntent.intentSender.let { intentSender ->
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Welcome Back")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password",) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),

            )
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                contentPadding = PaddingValues(vertical = 14.dp),
                onClick = { viewModel.loginWithEmail(email, password) },
                enabled = uiState !is LoginViewModel.UiState.Loading && googleSignInFlow !is Resource.Loading<BeginSignInResult>,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (uiState is LoginViewModel.UiState.Loading && googleSignInFlow !is Resource.Loading<BeginSignInResult>) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Login with Email",style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))


            Text("OR", modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                contentPadding = PaddingValues(vertical = 14.dp),
                onClick = { viewModel.beginGoogleSignIn() },
                enabled = uiState !is LoginViewModel.UiState.Loading && googleSignInFlow !is Resource.Loading<BeginSignInResult>,
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (googleSignInFlow is Resource.Loading<BeginSignInResult>) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary)
                } else {
                    Row (verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.Center){
                        Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = "")
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sign in with Google", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { navController.navigate(Screen.SignUp.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}