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
                            viewModel.saveOnboardingData()
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
