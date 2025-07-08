package com.chaosdev.devbuddy.ui.onboarding


import HourAndMinutePicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chaosdev.devbuddy.R
//import com.chaosdev.devbuddy.ui.common.CommuteMinuteOrHourPicker
import com.chaosdev.devbuddy.ui.navigation.Screen
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.grid.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune // A general icon for algorithms/settings
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.collections.mutableListOf

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
            .fillMaxSize(),
            //.padding(16.dp),
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
    ) {
         Row {
             Image(
                 painter = painterResource(id = R.drawable.welcome),
                 contentDescription = "Welcome Image",
                 modifier = Modifier.fillMaxWidth(fraction = 1f)
             )
         }
        Spacer(modifier = Modifier.height(24.dp))
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(bottom = 25.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.Top
        ){
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

@Composable
fun CommitmentPage(
    selectedTime: Int,
    onTimeSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    var selectedTime by remember { mutableStateOf("01:30") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Daily commitment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            )

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
                    selectedTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
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
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 13.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row (modifier = Modifier.weight(1f)){
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
fun NotificationPagePreview(

) {

    WelcomePage {  }

/*NotificationPage(notificationEnabled = true,
        onNotificationToggled = { },
        onFinish = {

        })*//*


    var currentSelectedTopics by remember { mutableStateOf(emptyList<String>()) }

    TopicsPage(
        selectedTopics = currentSelectedTopics,
        onTopicsSelected = { newSelection ->
            currentSelectedTopics = newSelection

        },
        onNext = {

        }
    )
    */
}
