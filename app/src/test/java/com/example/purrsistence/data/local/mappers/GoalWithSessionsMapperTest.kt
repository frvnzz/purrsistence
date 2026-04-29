package com.example.purrsistence.data.local.mappers

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.relation.GoalWithSessionsEntity
import com.example.purrsistence.domain.model.types.GoalType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class GoalWithSessionsMapperTest {

    @Test
    fun goalWithSessionsEntity_toDomain_mapsCorrectly() {
        val relation = GoalWithSessionsEntity(
            goalEntity = GoalEntity(
                goalId = 5,
                userId = 1,
                title = "Write Thesis",
                type = "MONTHLY",
                targetDuration = 300,
                deepFocus = true,
                inactive = false,
                createdAt = 1_700_000_000_000L,
                isCompleted = false
            ),
            sessions = listOf(
                TrackingSessionEntity(
                    trackingId = 1,
                    goalId = 5,
                    userId = 1,
                    pauseReminder = false,
                    deepFocus = false,
                    startTime = 1_700_000_000_000L,
                    endTime = 1_700_000_120_000L
                ),
                TrackingSessionEntity(
                    trackingId = 2,
                    goalId = 5,
                    userId = 1,
                    pauseReminder = false,
                    deepFocus = true,
                    startTime = 1_700_000_200_000L,
                    endTime = 1_700_000_320_000L
                )
            )
        )

        val domain = relation.toDomain()

        assertEquals(5, domain.goal.id)
        assertEquals("Write Thesis", domain.goal.title)
        assertEquals(GoalType.MONTHLY, domain.goal.type)
        assertEquals(Duration.ofMinutes(300), domain.goal.targetDuration)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.goal.createdAt)
        assertEquals(2, domain.sessions.size)
        assertEquals(1, domain.sessions[0].id)
        assertEquals(2, domain.sessions[1].id)
    }
}