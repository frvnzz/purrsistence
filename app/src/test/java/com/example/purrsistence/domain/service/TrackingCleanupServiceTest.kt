package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeGoalRepository
import com.example.purrsistence.data.local.repository.FakeTrackingRepository
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.TrackingCleanupService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class TrackingCleanupServiceTest {

    @Test
    fun cleanup_withNoInactiveGoals_doesNothingAndDoesNotCrash() = runBlocking {
        val goalRepository = FakeGoalRepository()
        val trackingRepository = FakeTrackingRepository()
        val timeProvider = FakeTimeProvider(Instant.parse("2026-05-01T00:00:00Z"))

        val service = TrackingCleanupService(
            goalRepository = goalRepository,
            trackingRepository = trackingRepository,
            timeProvider = timeProvider
        )

        service.cleanupInactiveGoalsAndOldSessions()

        assertEquals(0, trackingRepository.cleanupCalls)
        assertEquals(0, goalRepository.deleteCalls)
    }

    @Test
    fun cleanup_deletesInactiveGoal_whenAllOldSessionsAreRemoved() = runBlocking {
        val goalRepository = FakeGoalRepository()
        val trackingRepository = FakeTrackingRepository()
        val timeProvider = FakeTimeProvider(Instant.parse("2026-05-01T00:00:00Z"))

        goalRepository.seedGoal(
            Goal(
                id = 1,
                userId = 1,
                title = "Archived Goal",
                type = GoalType.WEEKLY,
                targetDuration = Duration.ofMinutes(60),
                deepFocus = false,
                inactive = true,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        trackingRepository.seedSession(
            TrackingSession(
                id = 1,
                goalId = 1,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-03-01T10:00:00Z"),
                endTime = Instant.parse("2026-03-01T11:00:00Z")
            )
        )

        trackingRepository.seedSession(
            TrackingSession(
                id = 2,
                goalId = 1,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-03-02T10:00:00Z"),
                endTime = Instant.parse("2026-03-02T11:00:00Z")
            )
        )

        val service = TrackingCleanupService(
            goalRepository = goalRepository,
            trackingRepository = trackingRepository,
            timeProvider = timeProvider
        )

        service.cleanupInactiveGoalsAndOldSessions()

        assertEquals(0, trackingRepository.getSessionsForGoal(1).size)
        assertNull(goalRepository.getStoredGoal(1))
        assertEquals(1, goalRepository.deleteCalls)
    }

    @Test
    fun cleanup_keepsInactiveGoal_whenSomeSessionsRemain() = runBlocking {
        val goalRepository = FakeGoalRepository()
        val trackingRepository = FakeTrackingRepository()
        val timeProvider = FakeTimeProvider(Instant.parse("2026-05-01T00:00:00Z"))

        goalRepository.seedGoal(
            Goal(
                id = 2,
                userId = 1,
                title = "Archived Goal",
                type = GoalType.WEEKLY,
                targetDuration = Duration.ofMinutes(60),
                deepFocus = false,
                inactive = true,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        trackingRepository.seedSession(
            TrackingSession(
                id = 1,
                goalId = 2,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-03-01T10:00:00Z"),
                endTime = Instant.parse("2026-03-01T11:00:00Z")
            )
        )

        trackingRepository.seedSession(
            TrackingSession(
                id = 2,
                goalId = 2,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-04-20T10:00:00Z"),
                endTime = Instant.parse("2026-04-20T11:00:00Z")
            )
        )

        val service = TrackingCleanupService(
            goalRepository = goalRepository,
            trackingRepository = trackingRepository,
            timeProvider = timeProvider
        )

        service.cleanupInactiveGoalsAndOldSessions()

        assertEquals(1, trackingRepository.getSessionsForGoal(2).size)
        assertNotNull(goalRepository.getStoredGoal(2))
        assertEquals(0, goalRepository.deleteCalls)
    }

    @Test
    fun cleanup_doesNotTouchActiveGoals() = runBlocking {
        val goalRepository = FakeGoalRepository()
        val trackingRepository = FakeTrackingRepository()
        val timeProvider = FakeTimeProvider(Instant.parse("2026-05-01T00:00:00Z"))

        goalRepository.seedGoal(
            Goal(
                id = 3,
                userId = 1,
                title = "Active Goal",
                type = GoalType.WEEKLY,
                targetDuration = Duration.ofMinutes(60),
                deepFocus = false,
                inactive = false,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        trackingRepository.seedSession(
            TrackingSession(
                id = 1,
                goalId = 3,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-03-01T10:00:00Z"),
                endTime = Instant.parse("2026-03-01T11:00:00Z")
            )
        )

        val service = TrackingCleanupService(
            goalRepository = goalRepository,
            trackingRepository = trackingRepository,
            timeProvider = timeProvider
        )

        service.cleanupInactiveGoalsAndOldSessions()

        assertNotNull(goalRepository.getStoredGoal(3))
        assertEquals(1, trackingRepository.getSessionsForGoal(3).size)
        assertEquals(0, goalRepository.deleteCalls)
    }
}