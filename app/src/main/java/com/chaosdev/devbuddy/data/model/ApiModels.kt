package com.chaosdev.devbuddy.data.model

import com.google.gson.annotations.SerializedName

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