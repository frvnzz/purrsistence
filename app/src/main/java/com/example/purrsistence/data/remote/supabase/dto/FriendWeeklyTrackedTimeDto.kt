package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendWeeklyTrackedTimeParamsDto(
    @SerialName("friend_user_id")
    val friendUserId: String,

    @SerialName("week_start")
    val weekStart: String,

    @SerialName("week_end")
    val weekEnd: String
)

@Serializable
data class FriendWeeklyTrackedTimeDto(
    @SerialName("total_minutes")
    val totalMinutes: Long
)