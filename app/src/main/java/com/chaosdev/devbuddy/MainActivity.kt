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

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
    
        val splashscreen = installSplashScreen()
        var keepSplashScreen = true

    
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { viewModel.isLoading.value } 
        
        //enableEdgeToEdge() 
        setContent {
            MyComposeApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
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
*/