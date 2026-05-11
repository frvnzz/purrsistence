package com.example.purrsistence.ui.state

data class TrackingUiState(
    val trackingId: Int? = null,
    val goalId: Int? = null,
    val startTime: java.time.Instant? = null,
    val elapsedMillis: Long = 0L,
    val isTracking: Boolean = false,

    // UI to show rewards (+ multiplier) after stopping a tracking session
    val rewardedCurrency: Int? = null,
    val multiplier: Double? = null,
    val sessionDurationMillis: Long? = null,
    val goalCompletionReward: Int? = null, //Additional reward for completing a goal, if applicable
)