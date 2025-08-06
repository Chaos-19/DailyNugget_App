package com.chaosdev.devbuddy.data.model

import com.google.gson.annotations.SerializedName


data class ApiKey(
    @SerializedName("id") val id: String,
    @SerializedName("key") val key: String,
    @SerializedName("userId") val userId: String
)

data class User(
    @SerializedName("id") val id: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String,
    @SerializedName("selected_topics") val selectedTopics: List<String> = emptyList(),
    @SerializedName("read_time") val readTime: Int = 0

)

data class UserResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User ,
    @SerializedName("apiKey") val apiKey: ApiKey
)

data class updatePreferencesRespons(
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String? = null
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

