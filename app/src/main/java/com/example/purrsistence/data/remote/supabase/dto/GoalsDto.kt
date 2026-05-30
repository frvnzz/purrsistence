package com.example.purrsistence.data.remote.supabase.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoalsDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("goal_id")
    val goalId: Int,

    val title: String,
    val type: String,

    @SerialName("target_duration")
    val targetDuration: Int,

    @SerialName("deep_focus")
    val deepFocus: Boolean,

    val inactive: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("is_completed")
    val isCompleted: Boolean,

    @SerialName("last_completed_at")
    val lastCompletedAt: String? = null
)