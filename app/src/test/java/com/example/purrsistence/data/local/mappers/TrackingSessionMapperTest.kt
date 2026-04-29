package com.example.purrsistence.data.local.mappers

import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.TrackingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TrackingSessionMapperTest {

    @Test
    fun trackingSessionEntity_toDomain_mapsCorrectly_withEndTime() {
        val entity = TrackingSessionEntity(
            trackingId = 3,
            goalId = 7,
            userId = 1,
            pauseReminder = true,
            deepFocus = true,
            startTime = 1_700_000_000_000L,
            endTime = 1_700_000_300_000L
        )

        val domain = entity.toDomain()

        assertEquals(3, domain.id)
        assertEquals(7, domain.goalId)
        assertEquals(1, domain.userId)
        assertEquals(true, domain.pauseReminder)
        assertEquals(true, domain.deepFocus)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.startTime)
        assertEquals(Instant.ofEpochMilli(1_700_000_300_000L), domain.endTime)
    }

    @Test
    fun trackingSessionEntity_toDomain_mapsCorrectly_withNullEndTime() {
        val entity = TrackingSessionEntity(
            trackingId = 3,
            goalId = 7,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = 1_700_000_000_000L,
            endTime = null
        )

        val domain = entity.toDomain()

        assertEquals(3, domain.id)
        assertEquals(7, domain.goalId)
        assertEquals(1, domain.userId)
        assertEquals(false, domain.pauseReminder)
        assertEquals(false, domain.deepFocus)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.startTime)
        assertNull(domain.endTime)
    }

    @Test
    fun trackingSession_toEntity_mapsCorrectly() {
        val domain = TrackingSession(
            id = 3,
            goalId = 7,
            userId = 1,
            pauseReminder = true,
            deepFocus = false,
            startTime = Instant.ofEpochMilli(1_700_000_000_000L),
            endTime = Instant.ofEpochMilli(1_700_000_300_000L)
        )

        val entity = domain.toEntity()

        assertEquals(3, entity.trackingId)
        assertEquals(7, entity.goalId)
        assertEquals(1, entity.userId)
        assertEquals(true, entity.pauseReminder)
        assertEquals(false, entity.deepFocus)
        assertEquals(1_700_000_000_000L, entity.startTime)
        assertEquals(1_700_000_300_000L, entity.endTime)
    }
}