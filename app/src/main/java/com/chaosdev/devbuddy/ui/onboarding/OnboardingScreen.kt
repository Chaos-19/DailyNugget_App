package com.chaosdev.devbuddy.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chaosdev.devbuddy.R
import com.chaosdev.devbuddy.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val selectedTopics by viewModel.selectedTopics.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()

    // Back button handling
    BackHandler(enabled = true) {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        }
        // Do nothing on first page (or navigate to Login if needed)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                    selectedTime = selectedTime,
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

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Welcome Image",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
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
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsPage(
    selectedTopics: List<String>,
    onTopicsSelected: (List<String>) -> Unit,
    onNext: () -> Unit
) {
    val topicList = listOf("Tech", "Science", "Business", "Health", "Education", "Lifestyle")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Select Your Interests",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topicList.size) { index ->
                val topic = topicList[index]
                FilterChip(
                    selected = selectedTopics.contains(topic),
                    onClick = {
                        val newTopics = selectedTopics.toMutableList()
                        if (newTopics.contains(topic)) {
                            newTopics.remove(topic)
                        } else {
                            newTopics.add(topic)
                        }
                        onTopicsSelected(newTopics)
                    },
                    label = { Text(topic) }
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

@Composable
fun CommitmentPage(
    selectedTime: Int,
    onTimeSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val timeOptions = listOf(5, 10, 15, 20)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Daily Learning Commitment",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            timeOptions.forEach { time ->
                Button(
                    onClick = { onTimeSelected(time) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTime == time) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                ) {
                    Text("$time mins")
                }
            }
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
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Enable Notifications",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable daily reading prompt",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = notificationEnabled,
                onCheckedChange = onNotificationToggled
            )
        }
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish")
        }
    }
}