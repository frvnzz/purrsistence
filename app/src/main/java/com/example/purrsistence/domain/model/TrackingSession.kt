package com.example.purrsistence.domain.model

import java.time.Duration
import java.time.Instant

data class TrackingSession(
    val id: Int = 0,
    val goalId: Int,
    val userId: Int,
    val pauseReminder: Boolean,
    val deepFocus: Boolean,
    val startTime: Instant,
    val endTime: Instant?
) {
    fun duration(now: Instant): Duration {
        return Duration.between(startTime, endTime ?: now)
    }

    fun finishedDuration(): Duration? {
        val finishedAt = endTime ?: return null
        return Duration.between(startTime, finishedAt)
    }
}