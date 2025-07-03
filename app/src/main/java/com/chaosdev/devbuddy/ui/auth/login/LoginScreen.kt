package com.chaosdev.devbuddy.ui.auth.login;

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
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.chaosdev.devbuddy.ui.common.Resource
import androidx.navigation.NavController
import com.chaosdev.devbuddy.ui.navigation.Screen // Assuming you have this

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  navController: NavController,
  viewModel: LoginViewModel = hiltViewModel()
) {
  var email by remember {
    mutableStateOf("")
  }
  var password by remember {
    mutableStateOf("")
  }
  val context = LocalContext.current as Activity

  val loginState by viewModel.loginState.collectAsState()
  val googleSignInFlow by viewModel.googleSignInFlow.collectAsState()

  val navigateToRoute by viewModel.navigateToRoute.collectAsState() // Observe new navigation state

  // Observe navigation route changes
  LaunchedEffect(navigateToRoute) {
    navigateToRoute?.let {
      route -> navController.navigate(route) {
        // Pop up to login screen and make it inclusive to prevent going back
        popUpTo(Screen.Login.route) {
          inclusive = true
        }
      }
      viewModel.resetNavigationRoute()// Consume the navigation event
    }
  }

  val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult()
  ) {
    result ->
    if (result.resultCode == Activity.RESULT_OK) {
      try {
        val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
        viewModel.signInWithGoogleCredential(credential)
      } catch (e: Exception) {
        Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
        viewModel.resetLoginState() // Reset if there's an issue getting credential
      }
    } else {
      Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
      viewModel.resetLoginState() // Reset if cancelled
    }
  }

  // Observe login state
  LaunchedEffect(loginState) {
    when (loginState) {
      is Resource.Success -> {
        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
        navController.navigate(Screen.Home.route) {
          popUpTo(Screen.Login.route) {
            inclusive = true
          } // Clear back stack
        }
        viewModel.resetLoginState()
      }
      is Resource.Error -> {
        Toast.makeText(context, loginState.message, Toast.LENGTH_LONG).show()
        viewModel.resetLoginState()
      }
      is Resource.Loading -> {
        // Show loading indicator
      }
      is Resource.Idle -> {
        // Initial state
      }
    }
  }

  // Observe Google Sign-In flow for initiating the UI
  LaunchedEffect(googleSignInFlow) {
    when (googleSignInFlow) {
      is Resource.Success -> {
        val beginSignInResult = googleSignInFlow.data
        beginSignInResult?.pendingIntent?.let {
          val intentSenderRequest = IntentSenderRequest.Builder(it).build()
          googleSignInLauncher.launch(intentSenderRequest)
        }
        viewModel.resetGoogleSignInFlow() // Reset to avoid re-launching
      }
      is Resource.Error -> {
        Toast.makeText(context, googleSignInFlow.message, Toast.LENGTH_LONG).show()
        viewModel.resetGoogleSignInFlow()
      }
      is Resource.Loading -> {
        // Show loading indicator for Google
      }
      is Resource.Idle -> {
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
      onValueChange = {
        email = it
      },
      label = {
        Text("Email")
      },
      leadingIcon = {
        Icon(Icons.Default.Email, contentDescription = "Email")
      },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = password,
      onValueChange = {
        password = it
      },
      label = {
        Text("Password")
      },
      leadingIcon = {
        Icon(Icons.Default.Lock, contentDescription = "Password")
      },
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = {
        viewModel.signInWithEmail(email, password)
      },
      enabled = loginState !is Resource.Loading && googleSignInFlow !is Resource.Loading,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (loginState is Resource.Loading && !(googleSignInFlow is Resource.Loading)) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
      } else {
        Text("Login with Email")
      }
    }
    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        navController.navigate(Screen.SignUp.route)
      },
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
      onClick = {
        viewModel.beginGoogleSignIn()
      },
      enabled = loginState !is Resource.Loading && googleSignInFlow !is Resource.Loading,
      modifier = Modifier.fillMaxWidth(),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
      if (googleSignInFlow is Resource.Loading) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary)
      } else {
        Text("Sign in with Google")
      }
    }
  }
}