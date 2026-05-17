package com.example.purrsistence.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TrackingSessionTest {

    @Test
    fun getPauseIntervals_parsesCorrectly() {
        val session = TrackingSession(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = Instant.ofEpochMilli(1000),
            endTime = null,
            pauseHistory = "2000;1000-2000,3000-4000;10"
        )
        val intervals = session.getPauseIntervals()
        assertEquals(2, intervals.size)
        assertEquals(Instant.ofEpochMilli(1000), intervals[0].first)
        assertEquals(Instant.ofEpochMilli(2000), intervals[0].second)
    }

    @Test
    fun getCheckpointedCurrency_parsesCorrectly() {
        val session = TrackingSession(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = Instant.ofEpochMilli(1000),
            endTime = null,
            pauseHistory = "2000;1000-2000;42"
        )
        assertEquals(42, session.getCheckpointedCurrency())
    }

    @Test
    fun getEffectiveMinutesSinceLastReset_calculatesCorrectly() {
        val startTime = Instant.ofEpochMilli(0)
        val now = Instant.ofEpochMilli(30 * 60 * 1000) // 30 mins wall clock
        
        // 5 min pause in the middle
        val session = TrackingSession(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = startTime,
            endTime = null,
            pauseHistory = "300000;600000-900000;0"
        )
        
        assertEquals(25, session.getEffectiveMinutesSinceLastReset(now))
    }

    @Test
    fun getEffectiveMinutesSinceLastReset_afterReset_calculatesCorrectly() {
        val startTime = Instant.ofEpochMilli(0)
        val resetTime = Instant.ofEpochMilli(20 * 60 * 1000)
        val now = Instant.ofEpochMilli(30 * 60 * 1000)
        
        // Pause before reset should be ignored
        // Pause after reset should be subtracted
        val session = TrackingSession(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = startTime,
            endTime = null,
            pauseHistory = "660000;300000-600000,1500000-1560000;38",
            lastResetTime = resetTime
        )
        
        //Total elapsed time since reset: 10 mins (20 to 30)
        // Paused since reset: 1 min (25 to 26)
        // Effective: 9 mins
        assertEquals(9, session.getEffectiveMinutesSinceLastReset(now))
    }
}