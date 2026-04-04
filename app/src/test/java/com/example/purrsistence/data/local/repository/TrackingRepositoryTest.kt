package com.example.purrsistence.data.local.repository

import com.example.purrsistence.domain.time.FakeTimeProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TrackingRepositoryTest {

    @Test
    fun startTracking() = runBlocking {
        val fakeTimeProvider = FakeTimeProvider(1000L)
        val repository = FakeTrackingRepository(fakeTimeProvider)

        val session = repository.startTracking(
            goalId = 1,
            userId = 3,
            pauseReminder = false
        )

        assertEquals(1000L, session.startTime)
        assertNull(session.endTime)

        val activeSession = repository.getActiveTrackingSession(1)
        assertNotNull(activeSession)
        assertEquals(1000L, activeSession!!.startTime)
        assertNull(activeSession.endTime)
    }

    @Test
    fun stopTracking() = runBlocking {
        val fakeTimeProvider = FakeTimeProvider(1000L)
        val repository = FakeTrackingRepository(fakeTimeProvider)

        val started = repository.startTracking(
            goalId = 1,
            userId = 4,
            pauseReminder = false
        )

        fakeTimeProvider.currentTime = 5000L
        repository.stopTracking(started.trackingId)

        val stored= repository.getTrackingSessionById(started.trackingId)

        assertNotNull(stored)
        assertEquals(1000L, stored!!.startTime)
        assertEquals(5000L, stored.endTime)
    }
}