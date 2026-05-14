package com.example.purrsistence.ui.state

import java.time.Instant

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

    val isPaused: Boolean = false,
    val totalPausedMillis: Long = 0L, //total time spent in paused state
    val currentPauseStart: Instant? = null,
    val multiplierResetWarning: String? = null, //Message to show if multiplier was reset due to inactivity
    val pauseAutoStopWarning: String? = null //Warning when auto-stop is imminent due to prolonged pause
)
