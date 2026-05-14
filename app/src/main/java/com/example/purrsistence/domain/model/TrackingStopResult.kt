package com.example.purrsistence.domain.model

data class TrackingStopResult(
    val rewardedCurrency: Int,
    val multiplier: Double,
    val sessionDurationMillis: Long,
    val goalCompletionReward: Int = 0, // Additional reward for completing a goal, if applicable
    val totalPausedMillis: Long = 0L, //total time spent in paused state during the session
    val multiplierReset: Boolean = false //Indicates if the multiplier was reset due to inactivity
)