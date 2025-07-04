package com.chaosdev.devbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
//import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.chaosdev.devbuddy.ui.navigation.NavGraph
import com.chaosdev.devbuddy.ui.theme.MyComposeApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch 
import com.chaosdev.devbuddy.ui.splash.SplashViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
    
        val splashscreen = installSplashScreen()

    
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { splashViewModel.isLoading.value } 
        
        //enableEdgeToEdge() 
        setContent {
            MyComposeApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //NavGraph()
                    NavGraph(splashViewModel = splashViewModel)
                }
            }
        }
    }
}

/*
package com.chaosdev.devbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chaosdev.devbuddy.ui.theme.MyComposeApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyComposeApplicationTheme {
        Greeting("Android")
    }
}
*/package com.chaosdev.devbuddy.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)package com.chaosdev.devbuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(primary = Purple80, secondary = PurpleGrey80,
        tertiary = Pink80)

private val LightColorScheme =
    lightColorScheme(primary = Purple40, secondary = PurpleGrey40,
        tertiary = Pink40

        /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */)

@Composable
fun MyComposeApplicationTheme(darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
                              dynamicColor: Boolean = true,
                              content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(
                context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window,
                view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography,
        content = content)
}package com.chaosdev.devbuddy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp,
        letterSpacing = 0.5.sp)/* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */)package com.chaosdev.devbuddy.ui;

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class DailyNuggetApplication : Application() {
    // You can initialize timber or other global setups here if needed
}package com.chaosdev.devbuddy.ui.auth.login;

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
}package com.chaosdev.devbuddy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class NavigationState {
        data object Onboarding : NavigationState()
        data object Home : NavigationState()
        data class GoogleSignIn(val beginSignInResult: BeginSignInResult) : NavigationState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.signInWithEmailAndPassword(email, password)) {
                is Result.Success -> {
                    checkOnboardingStatus()
                }
                is Result.Failure -> {
                    _uiState.value = UiState.Error(result.exception.message ?: "Login failed")
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.createUserWithEmailAndPassword(email, password)) {
                is Result.Success -> {
                    // New users typically need onboarding
                    _navigationState.value = NavigationState.Onboarding
                }
                is Result.Failure -> {
                    _uiState.value = UiState.Error(result.exception.message ?: "Signup failed")
                }
            }
        }
    }

    fun beginGoogleSignIn() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.beginGoogleSignIn()) {
                is Result.Success -> {
                    _navigationState.value = NavigationState.GoogleSignIn(result.data)
                }
                is Result.Failure -> {
                    _uiState.value = UiState.Error(result.exception.message ?: "Google Sign-In failed")
                }
            }
        }
    }

    fun completeGoogleSignIn(credential: SignInCredential) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.signInWithGoogle(credential)) {
                is Result.Success -> {
                    checkOnboardingStatus()
                }
                is Result.Failure -> {
                    _uiState.value = UiState.Error(result.exception.message ?: "Google Sign-In failed")
                }
            }
        }
    }

    private suspend fun checkOnboardingStatus() {
        val hasSeenOnboarding = onboardingPreferences.hasSeenOnboarding()
        _navigationState.value = if (hasSeenOnboarding) {
            NavigationState.Home
        } else {
            NavigationState.Onboarding
        }
    }
}package com.chaosdev.devbuddy.ui.auth.signup;

import android.widget.Toast
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
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val signUpState by viewModel.signUpState.collectAsState()

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is Resource.Success -> {
                Toast.makeText(context, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Go back to login screen
                viewModel.resetSignUpState()
            }
            is Resource.Error -> {
                Toast.makeText(context, signUpState.message, Toast.LENGTH_LONG).show()
                viewModel.resetSignUpState()
            }
            is Resource.Loading -> {
                // Show loading indicator
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
        Text(text = "Sign Up", style = MaterialTheme.typography.headlineLarge)
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
            onClick = { viewModel.signUpWithEmail(email, password) },
            enabled = signUpState !is Resource.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (signUpState is Resource.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Sign Up")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Login")
        }
    }
}package com.chaosdev.devbuddy.ui.auth.signup;


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
//import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.chaosdev.devbuddy.ui.common.Resource // Ensure this is imported

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val signUpState = _signUpState.asStateFlow()

    fun signUpWithEmail(email: String, password: String) {
        _signUpState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            if (result.isSuccess) { // <--- Change
                _signUpState.value = Resource.Success(result.getOrNull()!!)
            } else { // <--- Change
                _signUpState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = Resource.Idle()
    }
}package com.chaosdev.devbuddy.ui.common;

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Idle<T> : Resource<T>() // Initial state
}package com.chaosdev.devbuddy.ui.home;

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.navigation.Screen

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
                    popUpTo(Screen.Home.route) { inclusive = true } // Clear back stack
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to DevBuddy!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        currentUser?.let { user ->
            Text(text = "You are logged in as:", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${user.email ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            user.displayName?.let { name ->
                Text(text = "Name: $name", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        } ?: run {
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
}package com.chaosdev.devbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource // <--- Ensure this import is present
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged // Ensure this is imported
import kotlinx.coroutines.flow.map // Ensure this is imported

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val logoutState = _logoutState.asStateFlow()

    /*val currentUser: FirebaseUser?
        get() = authRepository.currentUser*/

    // Correctly observe the authStateChanges Flow from AuthRepository
    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateChanges
        .distinctUntilChanged() // Only emit if the user object actually changes (prevents redundant updates)
        .asStateFlow() // Convert to StateFlow. Its initial value will be the first emitted by authStateChanges (likely null, then the user).



    fun signOut() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signOut()
            result.onSuccess { // <--- Use .onSuccess and .onFailure
                _logoutState.value = Resource.Success(Unit)
            }.onFailure { exception ->
                _logoutState.value = Resource.Error(exception.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}
/*package com.chaosdev.devbuddy.ui.home;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val logoutState = _logoutState.asStateFlow()

    // Expose current user if needed, or handle in repository directly
    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun signOut() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is Result.Success -> {
                    _logoutState.value = Resource.Success(Unit)
                }
                is Result.Error -> {
                    _logoutState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed")
                }
                else -> { /* Handle other states */ }
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}
*/package com.chaosdev.devbuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chaosdev.devbuddy.ui.login.LoginScreen
import com.chaosdev.devbuddy.ui.login.LoginViewModel
import com.chaosdev.devbuddy.ui.splash.SplashScreen
import com.chaosdev.devbuddy.ui.splash.SplashViewModel
import com.chaosdev.devbuddy.ui.home.HomeScreen
import com.chaosdev.devbuddy.ui.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    splashViewModel: SplashViewModel,
    loginViewModel: LoginViewModel
) {
    val splashNavigationState = splashViewModel.navigationState.collectAsState().value
    val loginNavigationState = loginViewModel.navigationState.collectAsState().value

    val startDestination = when (splashNavigationState) {
        SplashViewModel.NavigationState.Login -> "login"
        SplashViewModel.NavigationState.Home -> "home"
        else -> "splash"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen()
        }
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onGoogleSignInResult = { credential ->
                    loginViewModel.completeGoogleSignIn(credential)
                }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    loginViewModel.viewModelScope.launch {
                        onboardingPreferences.setHasSeenOnboarding(true)
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen()
        }
    }

    LaunchedEffect(loginNavigationState) {
        when (val state = loginNavigationState) {
            is LoginViewModel.NavigationState.Onboarding -> {
                navController.navigate("onboarding") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginViewModel.NavigationState.Home -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginViewModel.NavigationState.GoogleSignIn -> {
                // Handle Google Sign-In result in LoginScreen
            }
            null -> Unit
        }
    }
}package com.chaosdev.devbuddy.ui.navigation

/**
 * Sealed class representing all the screens (routes) in the application.
 * Each object corresponds to a unique route string.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding_screen") 
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object Home : Screen("home_screen")

    // You can add more screens here as your application grows
    // For example:
    // object Profile : Screen("profile_screen")
    // object Settings : Screen("settings_screen")

    // If you need to pass arguments, you can define them here
    // object Detail : Screen("detail_screen/{id}") {
    //     fun createRoute(id: String) = "detail_screen/$id"
    // }
}package com.chaosdev.devbuddy.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
private val authRepository: AuthRepository
) : ViewModel() {

    sealed class NavigationState {
        data object Login : NavigationState()
        data object Home : NavigationState()
    }

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    init {
        viewModelScope.launch {
            // Simulate splash screen delay (optional)
            kotlinx.coroutines.delay(1000)
            // Check authentication state
            _navigationState.value = if (authRepository.currentUser != null) {
                NavigationState.Home
            } else {
                NavigationState.Login
            }
        }
    }
}package com.chaosdev.devbuddy.ui.onboarding;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted()
        }
    }
}package com.chaosdev.devbuddy.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chaosdev.devbuddy.ui.navigation.Screen // Import Screen

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to DevBuddy!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover amazing features and connect with other developers.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                viewModel.completeOnboarding() // Mark as complete
                // Navigate to Home after onboarding is complete
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                    // Also pop up to login if it's still on the back stack
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}
/*
package com.chaosdev.devbuddy.ui.onboarding;

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to DevBuddy!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover amazing features and connect with other developers.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                viewModel.completeOnboarding() // Mark as complete
                navController.navigate(com.chaosdev.devbuddy.ui.navigation.Screen.Login.route) {
                    popUpTo(com.chaosdev.devbuddy.ui.navigation.Screen.Onboarding.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}
*/package com.chaosdev.devbuddy.di

import android.app.Application
import android.content.Context // <--- Add this import
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences // <--- Add this import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideGoogleSignInClient(app: Application): SignInClient =
        Identity.getSignInClient(app.applicationContext)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        oneTapClient: SignInClient
    ): AuthRepository = AuthRepositoryImpl(auth, oneTapClient)

    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {
        return OnboardingPreferences(context)
    }
}
/*package com.chaosdev.devbuddy.di;

import android.app.Application
import android.content.Context 
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideGoogleSignInClient(app: Application): SignInClient =
        Identity.getSignInClient(app.applicationContext)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        oneTapClient: SignInClient
    ): AuthRepository = AuthRepositoryImpl(auth, oneTapClient)

    // If you add Use Cases, you'd provide them here as well
    // @Provides
    // @Singleton
    // fun provideLoginUseCase(repository: AuthRepository): LoginUseCase = LoginUseCase(repository)
    
    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {
        return OnboardingPreferences(context)
    }
}
*/package com.chaosdev.devbuddy.data.repository;

import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.identity.BeginSignInResult
import kotlinx.coroutines.flow.Flow 
/**
 * Defines the contract for authentication operations.
 */
interface AuthRepository {
    val currentUser: FirebaseUser?
    val authStateChanges: Flow<FirebaseUser?> 

    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(credential: SignInCredential): Result<FirebaseUser>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun beginGoogleSignIn(): Result<BeginSignInResult>
}package com.chaosdev.devbuddy.data.repository;

import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.chaosdev.devbuddy.R
import com.google.android.gms.auth.api.identity.BeginSignInResult
import kotlinx.coroutines.flow.Flow 
import kotlinx.coroutines.channels.awaitClose 

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient // For Google One Tap Sign-in
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser
        
    // NEW: Implement authStateChanges as a Flow
    override val authStateChanges: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser) // Send the current user whenever state changes
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener) // Clean up listener when flow is cancelled
        }
    }
        

    // --- Email/Password Authentication ---

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after creation"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after sign-in"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in with email/password: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- Google Authentication ---

    override suspend fun beginGoogleSignIn(): Result<BeginSignInResult> {
        return try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // This is your web client ID from Firebase project settings -> Project settings -> General
                        // It's NOT the Android client ID
                        .setServerClientId("260909717895-b496i0o3jju5uqn18iortbddgj35899d.apps.googleusercontent.com") // <--- IMPORTANT!
                        .setFilterByAuthorizedAccounts(false) // Set to true to show only authorized accounts
                        .build()
                )
                .setAutoSelectEnabled(true) // Automatically select the default account if available
                .build()

            val result = oneTapClient.beginSignIn(signInRequest).await()
            Result.success(result)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error beginning Google Sign-In: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(credential: SignInCredential): Result<FirebaseUser> {
        return try {
            val googleIdToken = credential.googleIdToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            authResult.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after Google sign-in"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in with Google: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- General Auth Operations ---

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            oneTapClient.signOut().await() // Sign out from Google One Tap as well
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing out: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending password reset email: ${e.message}", e)
            Result.failure(e)
        }
    }
}package com.chaosdev.devbuddy.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    suspend fun hasSeenOnboarding(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[HAS_SEEN_ONBOARDING] ?: false
        }.first()
    }

    suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = hasSeen
        }
    }
}