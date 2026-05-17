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
    val pauseHistory: String = "0;;0", //format: "totalPausedMillis;start1-end1,start2-end2;checkpointedCurrency"
    val currentPauseStart: Instant? = null, //when current pause has started, for ongoing pauses
    val lastResetTime: Instant? = null, //time of the last multiplier reset
) {
    //parser for getting the pause intervals from the pause history
    fun getPauseIntervals(): List<Pair<Instant, Instant>> {
        val segments = pauseHistory.split(";")
        val intervalsPart = segments.getOrNull(1) ?: return emptyList()
        if (intervalsPart.isEmpty()) return emptyList()
        return intervalsPart.split(",").mapNotNull {
            val parts = it.split("-")
            if (parts.size == 2) {
                val start = parts[0].toLongOrNull()?.let { Instant.ofEpochMilli(it) }
                val end = parts[1].toLongOrNull()?.let { Instant.ofEpochMilli(it) }
                if (start != null && end != null) start to end else null
            } else null
        }
    }

    //get the checkpointed currency from the pause history
    fun getCheckpointedCurrency(): Int {
        val segments = pauseHistory.split(";")
        return segments.getOrNull(2)?.toIntOrNull() ?: 0
    }

    //
    fun hasLongPause(now: Instant): Boolean {
        //check past intervals since lastResetTime (or startTime if null) TODO check if still needed?
        val resetLimit = lastResetTime ?: startTime
        if (getPauseIntervals().any { it.first.isAfter(resetLimit.minusMillis(1)) && Duration.between(it.first, it.second).toMinutes() >= 15 }) {
            return true
        }
        //check current ongoing pause
        currentPauseStart?.let {
            if (Duration.between(it, now).toMinutes() >= 15) return true
        }
        return false
    }

    //get total pause time from pause history (first number in string)
    fun getTotalPausedMillis(now: Instant): Long {
        val segments = pauseHistory.split(";")
        val baseTotal = segments.getOrNull(0)?.toLongOrNull() ?: 0L
        val currentAddition = currentPauseStart?.let {
            Duration.between(it, now).toMillis()
        } ?: 0L
        return baseTotal + currentAddition
    }

    //calculates time since the last multiplier reset has occured
    fun getEffectiveMinutesSinceLastReset(now: Instant): Int {
        val referenceTime = lastResetTime ?: startTime
        val totalElapsedDuration = Duration.between(referenceTime, now).toMillis()
        
        // Calculate paused time ONLY since referenceTime
        val pausedSinceReset = getPauseIntervals()
            .filter { it.first.isAfter(referenceTime.minusMillis(1)) }
            .sumOf { Duration.between(it.first, it.second).toMillis() }
        
        val currentPauseAddition = currentPauseStart?.let {
            if (it.isAfter(referenceTime.minusMillis(1))) {
                Duration.between(it, now).toMillis()
            } else 0L
        } ?: 0L

        val effectiveMillis = (totalElapsedDuration - pausedSinceReset - currentPauseAddition).coerceAtLeast(0L)
        return Duration.ofMillis(effectiveMillis).toMinutes().toInt()
    }

    fun effectiveDuration(now: Instant): Duration { //total duration minus paused time
        val end = endTime ?: now
        val totalDuration = Duration.between(startTime, end)

        return totalDuration.minusMillis(getTotalPausedMillis(end)).coerceAtLeast(Duration.ZERO)
    }

    fun duration(now: Instant): Duration {
        return Duration.between(startTime, endTime ?: now)
    }

    fun finishedDuration(): Duration? {
        val finishedAt = endTime ?: return null
        return effectiveDuration(finishedAt)
    }
}