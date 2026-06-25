package com.example.purrsistence.domain.model

import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.ui.util.TimeWindow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class GoalWithSessionsTest {

    private val baseGoal = Goal(
        id = 1,
        userId = 1,
        title = "Test Goal",
        type = GoalType.DAILY,
        targetDuration = Duration.ofHours(1),
        deepFocus = false,
        inactive = false,
        createdAt = Instant.parse("2026-06-01T00:00:00Z"),
        isCompleted = false,
        lastCompletedAt = null
    )

    @Test
    fun `totalTrackedDuration merges overlapping sessions`() {
        val now = Instant.parse("2026-06-01T10:00:00Z")
        val sessions = listOf(
            // Session 1: 08:00 - 09:00 (60 mins)
            TrackingSession(
                id = 1, goalId = 1, userId = 1, pauseReminder = false, deepFocus = false,
                startTime = Instant.parse("2026-06-01T08:00:00Z"),
                endTime = Instant.parse("2026-06-01T09:00:00Z")
            ),
            // Session 2: 08:30 - 09:30 (60 mins, overlaps 30 mins with Session 1)
            TrackingSession(
                id = 2, goalId = 1, userId = 1, pauseReminder = false, deepFocus = false,
                startTime = Instant.parse("2026-06-01T08:30:00Z"),
                endTime = Instant.parse("2026-06-01T09:30:00Z")
            )
        )
        val goalWithSessions = GoalWithSessions(baseGoal, sessions)

        // Total should be 08:00 to 09:30 = 90 minutes
        assertEquals(Duration.ofMinutes(90), goalWithSessions.totalTrackedDuration(now))
    }

    @Test
    fun `totalTrackedDuration handles pauses in overlapping sessions`() {
        val now = Instant.parse("2026-06-01T10:00:00Z")
        val sessions = listOf(
            // Session 1: 08:00 - 09:00, paused 08:15-08:45 (30 mins working)
            TrackingSession(
                id = 1, goalId = 1, userId = 1, pauseReminder = false, deepFocus = false,
                startTime = Instant.parse("2026-06-01T08:00:00Z"),
                endTime = Instant.parse("2026-06-01T09:00:00Z"),
                pauseHistory = "0;${Instant.parse("2026-06-01T08:15:00Z").toEpochMilli()}-${Instant.parse("2026-06-01T08:45:00Z").toEpochMilli()};0"
            ),
            // Session 2: 08:30 - 09:30 (60 mins working)
            // Session 2 covers the pause in Session 1
            TrackingSession(
                id = 2, goalId = 1, userId = 1, pauseReminder = false, deepFocus = false,
                startTime = Instant.parse("2026-06-01T08:30:00Z"),
                endTime = Instant.parse("2026-06-01T09:30:00Z")
            )
        )
        val goalWithSessions = GoalWithSessions(baseGoal, sessions)

        // Working intervals:
        // S1: [08:00-08:15], [08:45-09:00]
        // S2: [08:30-09:30]
        // Merged: [08:00-08:15], [08:30-09:30]
        // Total: 15 + 60 = 75 minutes
        assertEquals(Duration.ofMinutes(75), goalWithSessions.totalTrackedDuration(now))
    }

    @Test
    fun `trackedDurationInWindow clips sessions to window`() {
        val now = Instant.parse("2026-06-01T12:00:00Z")
        val window = TimeWindow(
            start = Instant.parse("2026-06-01T09:00:00Z"),
            end = Instant.parse("2026-06-01T10:00:00Z")
        )
        val sessions = listOf(
            // Session 1: 08:30 - 09:30 (starts before window, ends inside)
            TrackingSession(
                id = 1, goalId = 1, userId = 1, pauseReminder = false, deepFocus = false,
                startTime = Instant.parse("2026-06-01T08:30:00Z"),
                endTime = Instant.parse("2026-06-01T09:30:00Z")
            ),
            // Session 2: 09:45 - 10:15 (starts inside, ends after)
            TrackingSession(
                id = 2, goalId = 1, userId = 1, pauseReminder = false, deepFocus = false,
                startTime = Instant.parse("2026-06-01T09:45:00Z"),
                endTime = Instant.parse("2026-06-01T10:15:00Z")
            )
        )
        val goalWithSessions = GoalWithSessions(baseGoal, sessions)

        // Clipped intervals:
        // S1: [09:00-09:30] (30 mins)
        // S2: [09:45-10:00] (15 mins)
        // Total: 45 minutes
        assertEquals(Duration.ofMinutes(45), goalWithSessions.trackedDurationInWindow(window, now))
    }
}
