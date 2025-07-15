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
