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
}
package com.chaosdev.devbuddy.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        private val SELECTED_TOPICS = stringSetPreferencesKey("selected_topics")
        private val DAILY_COMMITMENT = intPreferencesKey("daily_commitment")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
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

    suspend fun setSelectedTopics(topics: List<String>) {
        dataStore.edit { preferences ->
            preferences[SELECTED_TOPICS] = topics.toSet()
        }
    }

    suspend fun getSelectedTopics(): Set<String> {
        return dataStore.data.map { preferences ->
            preferences[SELECTED_TOPICS] ?: emptySet()
        }.first()
    }

    suspend fun setDailyCommitment(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_COMMITMENT] = minutes
        }
    }

    suspend fun getDailyCommitment(): Int {
        return dataStore.data.map { preferences ->
            preferences[DAILY_COMMITMENT] ?: 10
        }.first()
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun isNotificationEnabled(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[NOTIFICATION_ENABLED] ?: false
        }.first()
    }
}
package com.chaosdev.devbuddy.data.model

import com.google.gson.annotations.SerializedName


data class User(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("selected_topics") val selectedTopics: List<String> = emptyList(),
    @SerializedName("read_time") val readTime: Int = 0
)

data class UserResponse(
    @SerializedName("user") val user: User ,
    @SerializedName("apiKey") val token: String
)

data class FeedItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)

data class Progress(
    @SerializedName("topics_completed") val topicsCompleted: Int,
    @SerializedName("hours_spent") val hoursSpent: Float
)

data class Challenge(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("difficulty") val difficulty: String
)

package com.chaosdev.devbuddy.data.network

import android.content.Context
import com.chaosdev.devbuddy.data.model.Challenge
import com.chaosdev.devbuddy.data.model.FeedItem
import com.chaosdev.devbuddy.data.model.Progress
import com.chaosdev.devbuddy.data.model.User
import com.chaosdev.devbuddy.data.model.UserResponse
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.common.Response
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.File
import javax.inject.Singleton

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body user: User): UserResponse

    @GET("api/feed")
    suspend fun getFeed(): List<FeedItem>

    @GET("api/progress")
    suspend fun getProgress(): Progress

    @GET("api/challenges")
    suspend fun getChallenges(): List<Challenge>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        val cache = Cache(File(context.cacheDir, "http-cache"), cacheSize.toLong())
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.dailynuggetapp.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
package com.chaosdev.devbuddy.data.repository

import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for authentication operations.
 */
interface AuthRepository {
    val currentUser: FirebaseUser?
    val authStateChanges: Flow<FirebaseUser?>

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser>

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(credential: SignInCredential): Result<FirebaseUser>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun beginGoogleSignIn(): Result<BeginSignInResult>
}
package com.chaosdev.devbuddy.data.repository

import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after creation"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
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
}
package com.chaosdev.devbuddy.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
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

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        val cache = Cache(File(context.cacheDir, "http-cache"), cacheSize.toLong())
        val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .build()
    }
}
package com.chaosdev.devbuddy.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class DailyNuggetApplication : Application() {
    // You can initialize timber or other global setups here if needed
}
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
package com.chaosdev.devbuddy.ui.login

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
                _uiState.value =
                    UiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
                _googleSignInFlow.value =
                    Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
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
                _uiState.value =
                    UiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
                _googleSignInFlow.value =
                    Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
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
}
package com.chaosdev.devbuddy.ui.auth.signup

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Create Your Account")
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
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.signUpWithEmail(email, password) },
                enabled = signUpState !is Resource.Loading,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                if (signUpState is Resource.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign Up", style = MaterialTheme.typography.titleMedium)
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
    }
}
package com.chaosdev.devbuddy.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import com.google.firebase.auth.FirebaseUser
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
                _signUpState.value =
                    Resource.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = Resource.Idle()
        _navigationState.value = null
    }
}
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaosdev.devbuddy.ui.common.WheelPicker

/**
 * A two-wheel picker for selecting hours and minutes.
 *
 * @param onTimeSelected A callback that provides the selected hour and minute.
 * @param initialHour The hour to be selected initially (0-23).
 * @param initialMinute The minute to be selected initially (0-59).
 */
@Composable
fun HourAndMinutePicker(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    initialHour: Int = 1,
    initialMinute: Int = 30
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    // Notify the parent composable when the selection changes.
    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeSelected(selectedHour, selectedMinute)
    }

    val itemHeight = 60.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        // The Box provides the central highlight background for the pickers.
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // The gray highlight "pill" in the background
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F3F5))
            )

            // The row containing the two pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Picker
                WheelPicker(
                    items = hours,
                    onItemSelected = { _, item -> selectedHour = item.toInt() },
                    initialIndex = hours.indexOf(initialHour.toString().padStart(2, '0')),
                    itemHeight = itemHeight
                )

                Text(
                    ":",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF2E3A59),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(bottom = 5.dp)
                )

                // Minute Picker
                WheelPicker(
                    items = minutes,
                    onItemSelected = { _, item -> selectedMinute = item.toInt() },
                    initialIndex = minutes.indexOf(initialMinute.toString().padStart(2, '0')),
                    itemHeight = itemHeight
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 320)
@Composable
fun HourAndMinutePickerPreview() {
    var selectedTime by remember { mutableStateOf("01:30") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HourAndMinutePicker(
            onTimeSelected = { hour, minute ->
                selectedTime =
                    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Selected Time: $selectedTime")
    }
}
package com.chaosdev.devbuddy.ui.common

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Idle<T> : Resource<T>() // Initial state
}
package com.chaosdev.devbuddy.ui.common

/**
 * A generic sealed class that represents the state of an operation,
 * typically used for asynchronous tasks like network requests.
 *
 * @param T The type of data expected in the successful case.
 */
sealed class Response<out T> { // Use 'out T' for covariance, allowing assignment to Response<Any> if needed

    /**
     * Represents a successful operation.
     * @param data The data returned by the operation.
     */
    data class Success<out T>(val data: T) : Response<T>()

    /**
     * Represents a failed operation.
     * @param message A descriptive message about the error.
     * @param throwable An optional Throwable that caused the error, for debugging or further processing.
     */
    data class Error(val message: String, val throwable: Throwable? = null) : Response<Nothing>()
    // Use Response<Nothing> because Error state doesn't hold data of type T

    /**
     * Represents an operation that is currently in progress.
     */
    object Loading : Response<Nothing>()
    // Use Response<Nothing> because Loading state doesn't hold data of type T
}
package com.chaosdev.devbuddy.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * A generic, reusable wheel-style picker.
 *
 * @param items The list of strings to display in the wheel.
 * @param onItemSelected A callback invoked when the user stops scrolling on a new item.
 * @param modifier Modifier for this composable.
 * @param initialIndex The index of the item to be centered initially.
 * @param itemHeight The height of each item in the wheel.
 * @param visibleItemsCount The number of items visible in the picker viewport.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    onItemSelected: (index: Int, item: String) -> Unit,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    itemHeight: Dp = 60.dp,
    visibleItemsCount: Int = 3
) {
    val listState = rememberLazyListState()
    val pickerHeight = itemHeight * visibleItemsCount

    // Find the index of the item that is closest to the center of the viewport.
    val centralItemIndex by remember {
        derivedStateOf {
            if (listState.layoutInfo.visibleItemsInfo.isEmpty() || listState.layoutInfo.viewportSize.height == 0) {
                -1
            } else {
                val viewportCenter =
                    listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize.height / 2
                listState.layoutInfo.visibleItemsInfo
                    .minByOrNull { (it.offset + it.size / 2 - viewportCenter).absoluteValue }
                    ?.index ?: -1
            }
        }
    }

    // Call the callback when scrolling stops and the central item is valid.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && centralItemIndex != -1 && items.isNotEmpty()) {
            onItemSelected(centralItemIndex, items[centralItemIndex])
        }
    }

    // Scroll to the initial item on the first composition.
    val density = LocalDensity.current
    LaunchedEffect(listState, items) {
        if (initialIndex in items.indices) {
            val pickerHeightPx = with(density) { pickerHeight.toPx() }
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val scrollOffset = (pickerHeightPx / 2 - itemHeightPx / 2).toInt()
            listState.scrollToItem(initialIndex, scrollOffset)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height(pickerHeight),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = pickerHeight / 2 - itemHeight / 2)
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == centralItemIndex
            Box(
                modifier = Modifier.height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF2E3A59) else Color.Gray
                    )
                )
            }
        }
    }
}
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
package com.chaosdev.devbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                _logoutState.value =
                    Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}
package com.chaosdev.devbuddy.ui.home.feed


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chaosdev.devbuddy.ui.common.Resource
import com.chaosdev.devbuddy.ui.home.HomeViewModel
import com.chaosdev.devbuddy.ui.home.feed.component.CardComponent


data class ArticleFeeds(
    val title:String,
    val time:String,
)

@Composable
fun FeedScreen(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    logoutState: Resource<Unit>,
    viewModel: HomeViewModel
) {

    val listOfFeed = mutableListOf(
        ArticleFeeds("Build A Full-Stack app with next 13.54","1hr"),
        ArticleFeeds("React Native Crash Course","38 min"),
        ArticleFeeds("The New use() hook React 19","1hr 40min"),
        ArticleFeeds("Getting Started with Kotlin","55 min"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Continue Learning", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                items = listOfFeed,
                key = { article -> article.title }
            ) { article ->
                CardComponent(title = article.title, time = article.time)
            }
        }

        /*Text(text = "Welcome to DevBuddy!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser != null) {
            Text(text = "You are logged in as:", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Email: ${currentUser.email ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
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
        }*/
    }
}
package com.chaosdev.devbuddy.ui.home.feed.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chaosdev.devbuddy.R

@Composable
fun CardComponent(
    title: String = "Build A Full-Stack app with next 13.54",
    time: String = "40 min"
) {

    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
        modifier = Modifier
            .size(width = 300.dp, height = 230.dp)

    ) {
        Column(
            modifier = Modifier
                .background(color = Color(34, 56, 40))
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.welcome),
                    contentDescription = "Welcome Image",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    //textAlign = TextAlign.Center,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CardComponentPreview() {
    CardComponent()
}
package com.chaosdev.devbuddy.ui.home.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SavedScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Saved", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Find your saved nuggets here.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
package com.chaosdev.devbuddy.ui.home.trending

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrendingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Trending", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Find trending nuggets here.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
package com.chaosdev.devbuddy.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person),
        BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination to avoid building up a large stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple instances of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
package com.chaosdev.devbuddy.ui.navigation

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
import com.chaosdev.devbuddy.ui.login.LoginViewModel
import com.chaosdev.devbuddy.ui.onboarding.OnboardingScreen
import com.chaosdev.devbuddy.ui.profile.ProfileScreen
import com.chaosdev.devbuddy.ui.settings.SettingsScreen
import com.chaosdev.devbuddy.ui.splash.SplashViewModel

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
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
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
}
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
package com.chaosdev.devbuddy.ui.onboarding

import HourAndMinutePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CommitmentPage(
    onTimeSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "How much time do you want to learn each day?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 16.dp)

            )
            Text(
                text = "This will help us recommend the right content for you.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(bottom = 16.dp)

            )

            HourAndMinutePicker(
                onTimeSelected = { hour, minute ->
                    val totalMinutes = hour * 60 + minute
                    onTimeSelected(totalMinutes)
                }
            )
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommitmentPagePreview() {
    CommitmentPage(onTimeSelected = {}, onNext = {})
}
package com.chaosdev.devbuddy.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NotificationPage(
    notificationEnabled: Boolean,
    onNotificationToggled: (Boolean) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "Enable daily reading prompt",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Get a daily prompt to read an A1-curated article",
                        style = MaterialTheme.typography.bodySmall,

                        )
                }
            }
            Switch(
                checked = notificationEnabled,
                onCheckedChange = onNotificationToggled
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onFinish,

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPagePreview() {
    NotificationPage(
        notificationEnabled = true,
        onNotificationToggled = {},
        onFinish = {}
    )
}
package com.chaosdev.devbuddy.ui.onboarding


import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.chaosdev.devbuddy.R
import com.chaosdev.devbuddy.ui.navigation.Screen
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val selectedTopics by viewModel.selectedTopics.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()

    // Back button handling
    BackHandler(enabled = true) {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        }

    }


    val TAGS = listOf("Welcome", "Topics", "Daily commitment", "Notification")

    Scaffold(

        topBar = {

            // We'll conditionally show the back button
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val showBackButton = navBackStackEntry?.destination?.route != Screen.Login.route

            CenterAlignedTopAppBar(


                title = {
                    Text(
                        TAGS[pagerState.currentPage],
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = {
                            // This will pop the current screen off the back stack
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack, // Standard back arrow
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )

        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )

                    1 -> TopicsPage(
                        selectedTopics = selectedTopics,
                        onTopicsSelected = { topics -> viewModel.updateSelectedTopics(topics) },
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                    )

                    2 -> CommitmentPage(
                        onTimeSelected = { time -> viewModel.updateSelectedTime(time) },
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        }
                    )

                    3 -> NotificationPage(
                        notificationEnabled = notificationEnabled,
                        onNotificationToggled = { enabled -> viewModel.toggleNotification(enabled) },
                        onFinish = {
                            //viewModel.saveOnboardingData()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                                popUpTo(Screen.Login.route) { inclusive = true }
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            // Page Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { index ->
                    Icon(
                        painter = painterResource(
                            if (pagerState.currentPage == index) R.drawable.ic_google else R.drawable.app_logo
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                    )
                }
            }
        }

    }

}
package com.chaosdev.devbuddy.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _selectedTopics = MutableStateFlow<List<String>>(emptyList())
    val selectedTopics: StateFlow<List<String>> = _selectedTopics.asStateFlow()

    private val _selectedTime = MutableStateFlow(10)
    val selectedTime: StateFlow<Int> = _selectedTime.asStateFlow()

    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    fun updateSelectedTopics(topics: List<String>) {
        _selectedTopics.value = topics
    }

    fun updateSelectedTime(time: Int) {
        _selectedTime.value = time
    }

    fun toggleNotification(enabled: Boolean) {
        _notificationEnabled.value = enabled
    }

    fun saveOnboardingData() {
        viewModelScope.launch {
            onboardingPreferences.setHasSeenOnboarding(true)
            onboardingPreferences.setSelectedTopics(_selectedTopics.value)
            onboardingPreferences.setDailyCommitment(_selectedTime.value)
            onboardingPreferences.setNotificationEnabled(_notificationEnabled.value)
        }
    }
}
package com.chaosdev.devbuddy.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsPage(
    selectedTopics: List<String>,
    onTopicsSelected: (List<String>) -> Unit,
    onNext: () -> Unit
) {
    // 1. Define a data class for your topics, including the icon
    data class TopicItem(
        val name: String,
        val icon: ImageVector
    )

    // 2. Create your list of topics with associated icons
    val topicList = remember {
        listOf(
            TopicItem("Frontend", Icons.Filled.Code), // Or Icons.Outlined.Code
            TopicItem("Backend", Icons.Filled.Dns), // Represents server/network
            TopicItem("DevOps", Icons.Filled.Cloud), // Cloud for DevOps/Cloud computing
            TopicItem("AI", Icons.Filled.SmartToy), // Robot for AI
            TopicItem("Databases", Icons.Filled.Storage), // Database icon
            TopicItem("Algorithms", Icons.Filled.Tune), // Gear/settings for algorithms (can vary)
            // Add more topics as needed, matching the image content
            TopicItem("Mobile Dev", Icons.Filled.Smartphone),
            TopicItem("Cybersecurity", Icons.Filled.Security),
            TopicItem("Data Science", Icons.Filled.Analytics),
            TopicItem("Gaming", Icons.Filled.SportsEsports),
            TopicItem("Cloud Computing", Icons.Filled.CloudQueue),
            TopicItem("Web Design", Icons.Filled.Palette)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "What are you interested in?", // Updated text
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Select topics to personalize your learning experience.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(bottom = 16.dp)

        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            //contentPadding = PaddingValues(2.dp),
            //verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topicList) { topicItem -> // Iterate directly over TopicItem objects
                val isSelected = selectedTopics.contains(topicItem.name)
                FilterChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    selected = isSelected,
                    onClick = {
                        val newTopics = selectedTopics.toMutableList()
                        if (newTopics.contains(topicItem.name)) {
                            newTopics.remove(topicItem.name)
                        } else {
                            newTopics.add(topicItem.name)
                        }
                        onTopicsSelected(newTopics)
                    },
                    label = { Text(topicItem.name) },
                    leadingIcon = { // This is where the icon integration happens
                        if (isSelected) {
                            // When selected, FilterChip usually shows a default checkmark
                            // You can keep it as is, or customize it if needed.
                            Icon(
                                imageVector = Icons.Filled.DoneAll,
                                contentDescription = "Selected",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        } else {
                            // When not selected, show the specific topic icon
                            Icon(
                                imageVector = topicItem.icon,
                                contentDescription = topicItem.name,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    },
                    // You can also customize `selectedIcon` if you want a different checkmark or animation
                    // selectedIcon = {
                    //     Icon(
                    //         imageVector = Icons.Filled.Done, // Another checkmark option
                    //         contentDescription = "Selected",
                    //         modifier = Modifier.size(FilterChipDefaults.IconSize)
                    //     )
                    // }
                )
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Next")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopicsPagePreview() {
    var currentSelectedTopics by remember { mutableStateOf(emptyList<String>()) }

    TopicsPage(
        selectedTopics = currentSelectedTopics,
        onTopicsSelected = { newSelection ->
            currentSelectedTopics = newSelection
        },
        onNext = {}
    )
}
package com.chaosdev.devbuddy.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chaosdev.devbuddy.R

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.welcome),

                contentDescription = "Welcome Image",
                modifier = Modifier.fillMaxWidth(fraction = 1f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(bottom = 25.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Welcome to DailyNugget",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Turn your commute into a learning journey",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Started")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePagePreview() {
    WelcomePage {}
}
package com.chaosdev.devbuddy.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chaosdev.devbuddy.ui.navigation.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
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
            Text(text = "Profile Screen", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "This is the profile page.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
package com.chaosdev.devbuddy.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chaosdev.devbuddy.ui.navigation.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
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
            Text(text = "Settings Screen", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "This is the settings page.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
package com.chaosdev.devbuddy.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
}
package com.chaosdev.devbuddy.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
package com.chaosdev.devbuddy.ui.theme

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
    darkColorScheme(
        primary = Purple80, secondary = PurpleGrey80,
        tertiary = Pink80
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40, secondary = PurpleGrey40,
        tertiary = Pink40

        /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
    )

@Composable
fun MyComposeApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(
                context
            ) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(
                window,
                view
            ).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography,
        content = content
    )
}
package com.chaosdev.devbuddy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )/* Other default text styles to override
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
    */
)
