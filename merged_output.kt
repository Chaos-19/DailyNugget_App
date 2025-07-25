package com.chaosdev.devbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.chaosdev.devbuddy.ui.navigation.AppNavGraph
import com.chaosdev.devbuddy.ui.splash.SplashViewModel
import com.chaosdev.devbuddy.ui.theme.MyComposeApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashscreen.setKeepOnScreenCondition { splashViewModel.isLoading.value }

        setContent {
            MyComposeApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLoading by splashViewModel.isLoading.collectAsState()
                    val navState by splashViewModel.navigationState.collectAsState()
                    
                    if (!isLoading && navState != null) {
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            splashViewModel = splashViewModel
                        )
                    }
                }
            }
        }
    }
}package com.chaosdev.devbuddy.ui.theme

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
}package com.chaosdev.devbuddy.ui.auth.login

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
}package com.chaosdev.devbuddy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
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
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class NavigationState {
        object Onboarding : NavigationState()
        object Home : NavigationState()
        data class GoogleSignIn(val beginSignInResult: BeginSignInResult) : NavigationState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _googleSignInFlow = MutableStateFlow<Resource<BeginSignInResult>>(Resource.Idle())
    val googleSignInFlow = _googleSignInFlow.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.signInWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                checkOnboardingStatus()
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                _navigationState.value = NavigationState.Onboarding
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun beginGoogleSignIn() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _googleSignInFlow.value = Resource.Loading()
            val result = authRepository.beginGoogleSignIn()
            if (result.isSuccess) {
                _googleSignInFlow.value = Resource.Success(result.getOrNull()!!)
                _navigationState.value = NavigationState.GoogleSignIn(result.getOrNull()!!)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
                _googleSignInFlow.value = Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
            }
        }
    }

    fun completeGoogleSignIn(credential: SignInCredential) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.signInWithGoogle(credential)
            if (result.isSuccess) {
                checkOnboardingStatus()
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
                _googleSignInFlow.value = Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
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

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun resetGoogleSignInFlow() {
        _googleSignInFlow.value = Resource.Idle()
    }
}package com.chaosdev.devbuddy.ui.auth.signup

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
    val navigationState by viewModel.navigationState.collectAsState()

    LaunchedEffect(navigationState) {
        when (navigationState) {
            is SignUpViewModel.NavigationState.Onboarding -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
                viewModel.resetSignUpState()
            }
            null -> Unit
        }
    }

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is Resource.Success -> {
                Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
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
}package com.chaosdev.devbuddy.ui.auth.signup

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
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class NavigationState {
        object Onboarding : NavigationState()
    }

    private val _signUpState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val signUpState = _signUpState.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    fun signUpWithEmail(email: String, password: String) {
        _signUpState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                _signUpState.value = Resource.Success(result.getOrNull()!!)
                _navigationState.value = NavigationState.Onboarding
            } else {
                _signUpState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = Resource.Idle()
        _navigationState.value = null
    }
}package com.chaosdev.devbuddy.ui.common;

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Idle<T> : Resource<T>() // Initial state
}package com.chaosdev.devbuddy.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.navigation.Screen
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val logoutState = viewModel.logoutState.collectAsState().value
    val currentUser = viewModel.currentUser.collectAsState().value

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is Resource.Success -> {
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
                viewModel.resetLogoutState()
            }
            is Resource.Success<Unit>  -> {
                Toast.makeText(context, logoutState.message, Toast.LENGTH_LONG).show()
                viewModel.resetLogoutState()
            }
            is Resource.Error<Unit> -> {
                Toast.makeText(context, logoutState.message, Toast.LENGTH_LONG).show()
                viewModel.resetLogoutState()
            }
            is Resource.Loading<Unit> -> {
                // Show loading indicator
            }
            is Resource.Idle<Unit> -> {
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

        if (currentUser != null) {
            Text(text = "You are logged in as:", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${currentUser?.email ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
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
}package com.chaosdev.devbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val logoutState = _logoutState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateChanges
    .distinctUntilChanged()
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun signOut() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signOut()
            if (result.isSuccess) {
                _logoutState.value = Resource.Success(Unit)
            } else {
                _logoutState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}package com.chaosdev.devbuddy.ui.navigation

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class NavigationState {
        object Login : NavigationState()
        object Home : NavigationState()
    }

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState: StateFlow<NavigationState?> = _navigationState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateChanges
                .distinctUntilChanged()
                .map { user ->
                    if (user != null) NavigationState.Home else NavigationState.Login
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
                .collect { state ->
                    _navigationState.value = state
                    _isLoading.value = false
                }
        }
    }
}package com.chaosdev.devbuddy.ui.onboarding

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
            onboardingPreferences.setHasSeenOnboarding(true)
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
import com.chaosdev.devbuddy.ui.navigation.Screen

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
                viewModel.completeOnboarding()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                    popUpTo(Screen.Login.route) { inclusive = true }
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}package com.chaosdev.devbuddy.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

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
        return OnboardingPreferences(context.dataStore)
    }
}package com.chaosdev.devbuddy.data.repository;

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
import kotlinx.coroutines.flow.first
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