package com.example.purrsistence.ui.tracking

data class TrackingUiState(
    val trackingId: Int? = null,
    val goalId: Int? = null,
    val startTime: Long? = null,
    val elapsedMillis: Long = 0L,
    val isTracking: Boolean = false
)