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
