package com.example.purrsistence.domain.model

data class TrackingStopResult(
    val rewardedCurrency: Int,
    val multiplier: Double,
    val sessionDurationMillis: Long,
    val goalCompletionReward: Int = 0 // Additional reward for completing a goal, if applicable
)