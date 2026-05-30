package com.example.purrsistence.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.purrsistence.domain.model.TrackingSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TrackingRepositoryRoomIntegrationTest : RoomIntegrationTestBase() {

    @Test
    fun insertAndFinishTrackingSession_persistsEndTime() = runBlocking {
        seedUserEntity(userId = 1)
        seedGoalEntity(goalId = 1, userId = 1)

        val inserted = trackingRepository.insertTrackingSession(
            TrackingSession(
                id = 0,
                goalId = 1,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.ofEpochMilli(1_000L),
                endTime = null
            )
        )

        val active = trackingRepository.getActiveTrackingSession(1)
        assertNotNull(active)
        assertEquals(inserted.id, active!!.id)
        assertNull(active.endTime)

        val finished = trackingRepository.finishTrackingSession(
            trackingId = inserted.id,
            endTimeMillis = 5_000L
        )

        assertNotNull(finished)
        assertEquals(Instant.ofEpochMilli(5_000L), finished!!.endTime)

        val afterFinish = trackingRepository.getActiveTrackingSession(1)
        assertNull(afterFinish)
    }

    @Test
    fun deleteFinishedSessionsForGoalBefore_removesOnlyOldFinishedSessions() = runBlocking {
        seedUserEntity(userId = 1)
        seedGoalEntity(goalId = 1, userId = 1)

        val oldFinished = trackingRepository.insertTrackingSession(
            TrackingSession(
                id = 0,
                goalId = 1,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-03-01T10:00:00Z"),
                endTime = Instant.parse("2026-03-01T11:00:00Z")
            )
        )

        val recentFinished = trackingRepository.insertTrackingSession(
            TrackingSession(
                id = 0,
                goalId = 1,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-04-20T10:00:00Z"),
                endTime = Instant.parse("2026-04-20T11:00:00Z")
            )
        )

        val active = trackingRepository.insertTrackingSession(
            TrackingSession(
                id = 0,
                goalId = 1,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = Instant.parse("2026-04-25T10:00:00Z"),
                endTime = null
            )
        )

        trackingRepository.deleteFinishedSessionsForGoalBefore(
            goalId = 1,
            cutoff = Instant.parse("2026-04-01T00:00:00Z")
        )

        assertNull(trackingRepository.getTrackingSessionById(oldFinished.id))
        assertNotNull(trackingRepository.getTrackingSessionById(recentFinished.id))
        assertNotNull(trackingRepository.getTrackingSessionById(active.id))
        assertEquals(2, trackingRepository.countSessionsForGoal(1))
    }

    @Test
    fun deleteAllTrackingSessions_removesAllSessionsForUser() = runBlocking {
        seedUserEntity(userId = 1)
        seedGoalEntity(goalId = 1, userId = 1)
        seedTrackingSessionEntity(sessionId = 1, goalId = 1, userId = 1, duration = 1000)
        seedTrackingSessionEntity(sessionId = 2, goalId = 1, userId = 1, duration = 2000)

        seedUserEntity(userId = 2)
        seedGoalEntity(goalId = 2, userId = 2)
        seedTrackingSessionEntity(sessionId = 3, goalId = 2, userId = 2, duration = 3000)

        assertEquals(2, trackingRepository.getTrackingSessionsForSync(1).size)
        assertEquals(1, trackingRepository.getTrackingSessionsForSync(2).size)

        trackingRepository.deleteAllTrackingSessions(1)

        assertEquals(0, trackingRepository.getTrackingSessionsForSync(1).size)
        assertEquals(1, trackingRepository.getTrackingSessionsForSync(2).size)
    }
}