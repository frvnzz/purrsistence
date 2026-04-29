package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeGoalRepository
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.GoalService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class GoalServiceTest {

    @Test
    fun createGoal_buildsCanonicalGoalCorrectly() = runBlocking {
        val repository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val service = GoalService(repository, timeProvider)

        service.createGoal(
            userId = 1,
            title = "TestGoal",
            type = "weekly",
            targetMinutes = 120,
            deepFocus = true,
            inactive = false,
            isCompleted = false
        )

        val goal = repository.getGoal(1).first()

        assertNotNull(goal)
        assertEquals("TestGoal", goal!!.title)
        assertEquals(GoalType.WEEKLY, goal.type)
        assertEquals(Duration.ofMinutes(120), goal.targetDuration)
        assertEquals(Instant.ofEpochMilli(1000L), goal.createdAt)
    }
}