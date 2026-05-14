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
    val endTime: Instant?,
    val pausedTimeMillis: Long = 0L, //total paused time
    val currentPauseStart: Instant? = null, //when current pause has started, for ongoing pauses
    val pauseIntervals: List<Pair<Instant, Instant>> = emptyList(), //list of pause intervals (start, end)
) {
    fun effectiveDuration(now: Instant): Duration { //total duration minus paused time
        val end = endTime ?: now
        val totalDuration = Duration.between(startTime, end)

        val currentPauseDuration = currentPauseStart?.let {
            Duration.between(it, end)
        } ?: Duration.ZERO

        return totalDuration.minusMillis(pausedTimeMillis).minus(currentPauseDuration).coerceAtLeast(Duration.ZERO)
    }

    fun duration(now: Instant): Duration {
        return Duration.between(startTime, endTime ?: now)
    }

    fun finishedDuration(): Duration? {
        val finishedAt = endTime ?: return null
        return effectiveDuration(finishedAt)
    }
}