package com.example.purrsistence.data.local.mappers

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.types.GoalType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class GoalMapperTest {

    @Test
    fun goalEntity_toDomain_mapsCorrectly() {
        val entity = GoalEntity(
            goalId = 7,
            userId = 1,
            title = "Read Papers",
            type = "WEEKLY",
            targetDuration = 120,
            deepFocus = true,
            inactive = false,
            createdAt = 1_700_000_000_000L,
            isCompleted = false
        )

        val domain = entity.toDomain()

        assertEquals(7, domain.id)
        assertEquals(1, domain.userId)
        assertEquals("Read Papers", domain.title)
        assertEquals(GoalType.WEEKLY, domain.type)
        assertEquals(Duration.ofMinutes(120), domain.targetDuration)
        assertEquals(true, domain.deepFocus)
        assertEquals(false, domain.inactive)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.createdAt)
        assertEquals(false, domain.isCompleted)
    }

    @Test
    fun goal_toEntity_mapsCorrectly() {
        val goal = Goal(
            id = 7,
            userId = 1,
            title = "Read Papers",
            type = GoalType.WEEKLY,
            targetDuration = Duration.ofMinutes(120),
            deepFocus = true,
            inactive = false,
            createdAt = Instant.ofEpochMilli(1_700_000_000_000L),
            isCompleted = false
        )

        val entity = goal.toEntity()

        assertEquals(7, entity.goalId)
        assertEquals(1, entity.userId)
        assertEquals("Read Papers", entity.title)
        assertEquals("WEEKLY", entity.type)
        assertEquals(120, entity.targetDuration)
        assertEquals(true, entity.deepFocus)
        assertEquals(false, entity.inactive)
        assertEquals(1_700_000_000_000L, entity.createdAt)
        assertEquals(false, entity.isCompleted)
    }
}