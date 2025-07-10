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