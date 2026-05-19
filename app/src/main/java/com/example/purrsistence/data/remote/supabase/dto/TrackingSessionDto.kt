package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackingSessionDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("tracking_id")
    val trackingId: Int,

    @SerialName("goal_id")
    val goalId: Int,

    @SerialName("pause_reminder")
    val pauseReminder: Boolean,

    @SerialName("deep_focus")
    val deepFocus: Boolean,

    @SerialName("start_time")
    val startTime: String,

    @SerialName("end_time")
    val endTime: String? = null
)