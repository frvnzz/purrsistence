package com.example.purrsistence.domain.model

data class TrackingStopResult(
    val rewardedCurrency: Int,
    val multiplier: Double,
    val sessionDurationMillis: Long
)