package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeGoalRepository
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalWithSessions
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.GoalService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

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


    //Goal Completion Tests

    @Test
    fun completeGoalIfReached_marksGoalCompleted_whenTargetReached() = runBlocking {
        val repository = FakeGoalRepository()

        val now = Instant.parse("2026-05-07T12:00:00Z") //Set up time provider to return fixed "now" for testing
        val timeProvider = FakeTimeProvider(now)
        val zone =  ZoneId.of("Europe/Vienna")

        val service = GoalService(repository, timeProvider)

        //create goal with 60 min target
        service.createGoal(
            userId = 1,
            title = "Study",
            type = "daily",
            targetMinutes = 60,
            deepFocus = false,
            inactive = false,
            isCompleted = false
        )

        val goal = repository.getGoal(1).first()!!

        //create 60 min tracking session
        val session = TrackingSession(
            id = 1,
            goalId = goal.id,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = now.minus(Duration.ofMinutes(60)),
            endTime = now
        )

        val goalWithSessions = GoalWithSessions(
            goal = goal,
            sessions = listOf(session)
        )

        val result = service.completeGoalIfReached( //Call the method under test
            goalWithSessions = goalWithSessions,
            now = now.atZone(zone)
        )

        val updatedGoal = repository.getGoal(goal.id).first()!! //Fetch the updated goal to verify changes

        assertTrue(result) //Method should return true indicating goal was completed
        assertTrue(updatedGoal.isCompleted) //Goal should be marked as completed
        assertEquals(now, updatedGoal.lastCompletedAt) //lastCompletedAt should be updated to "now"
    }

    @Test
    fun completeGoalIfReached_returnsFalse_whenTargetNotReached() = runBlocking { //similar test but without reaching target to verify functionality
        val repository = FakeGoalRepository()

        val now = Instant.parse("2026-05-07T12:00:00Z")
        val timeProvider = FakeTimeProvider(now)

        val zone =  ZoneId.of("Europe/Vienna")

        val service = GoalService(repository, timeProvider)

        service.createGoal(
            userId = 1,
            title = "Study",
            type = "daily",
            targetMinutes = 120,
            deepFocus = false,
            inactive = false,
            isCompleted = false
        )

        val goal = repository.getGoal(1).first()!!

        // only 30 min session and target instead being 120min
        val session = TrackingSession(
            id = 1,
            goalId = goal.id,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = now.minus(Duration.ofMinutes(30)),
            endTime = now
        )

        val goalWithSessions = GoalWithSessions(
            goal = goal,
            sessions = listOf(session)
        )

        val result = service.completeGoalIfReached(
            goalWithSessions,
            now.atZone(zone)
        )

        val updatedGoal = repository.getGoal(goal.id).first()!!

        assertFalse( result) //should be false
        assertFalse(updatedGoal.isCompleted) //goal should not be marked complete
        assertNull(updatedGoal.lastCompletedAt) //since goal was never completed before lastCompletedAt should remain null
    }

    @Test
    fun alreadyCompletedCurrentWindow_returnsFalse() = runBlocking {
        val repository = FakeGoalRepository()

        val now = Instant.parse("2026-05-07T12:00:00Z")
        val timeProvider = FakeTimeProvider(now)

        val zone =  ZoneId.of("Europe/Vienna")

        val service = GoalService(repository, timeProvider)

        // Goal that has already been completed in the current time window (e.g., today for a daily goal)
        val alreadyCompletedGoal = Goal(
            id = 1,
            userId = 1,
            title = "Study",
            type = GoalType.DAILY,
            targetDuration = Duration.ofMinutes(60),
            deepFocus = false,
            inactive = false,
            createdAt = now.minus(Duration.ofDays(1)),
            isCompleted = true,
            lastCompletedAt = now.minus(Duration.ofHours(1))
        )

        repository.insertGoal(alreadyCompletedGoal) //add goal to repository

        // session that also meets target
        val session = TrackingSession(
            id = 1,
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = now.minus(Duration.ofMinutes(60)),
            endTime = now
        )

        val goalWithSessions = GoalWithSessions(
            goal = alreadyCompletedGoal,
            sessions = listOf(session)
        )

        val result = service.completeGoalIfReached(
            goalWithSessions,
            now.atZone(zone)
        )

        val unchangedGoal = repository.getGoal(1).first()!!

        // should return false since goal was already completed in the current time window
        assertFalse(result)

        // goal should remain unchanged and still completed
        assertTrue( unchangedGoal.isCompleted)
        assertEquals( //lastCompletedAt should remain the same since goal was already completed in the current window and method should not update it
            now.minus(Duration.ofHours(1)),
            unchangedGoal.lastCompletedAt
        )
    }
}