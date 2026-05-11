package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalWithSessions
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

class GoalService(
    private val goalRepository: GoalRepository,
    private val timeProvider: TimeProvider
) {

    fun getGoals(userId: Int) =
        goalRepository.getGoals(userId)

    fun getGoal(goalId: Int?): Flow<Goal?> =
        if (goalId == null) flowOf(null)
        else goalRepository.getGoal(goalId)

    suspend fun createGoal(
        userId: Int,
        title: String,
        type: String,
        targetMinutes: Int,
        deepFocus: Boolean,
        inactive: Boolean,
        isCompleted: Boolean
    ) {
        val goal = Goal(
            id = 0,
            userId = userId,
            title = title,
            type = GoalType.valueOf(type.uppercase()),
            targetDuration = Duration.ofMinutes(targetMinutes.toLong()),
            deepFocus = deepFocus,
            inactive = inactive,
            createdAt = timeProvider.now(),
            isCompleted = isCompleted,
            lastCompletedAt = null
        )

        goalRepository.insertGoal(goal)
    }

    suspend fun updateGoal(
        goalId: Int,
        title: String,
        type: GoalType,
        targetMinutes: Int,
        deepFocus: Boolean
    ) {
        val currentGoal = goalRepository.getGoal(goalId).firstOrNull() ?: return

        val updatedGoal = currentGoal.copy(
            title = title,
            type = type,
            targetDuration = Duration.ofMinutes(targetMinutes.toLong()),
            deepFocus = deepFocus
        )

        goalRepository.updateGoal(updatedGoal)
    }

    suspend fun deleteGoal(goalId: Int) {
        val currentGoal = goalRepository.getGoal(goalId).firstOrNull() ?: return
        if (currentGoal.inactive) return

        val inactiveGoal = currentGoal.copy(inactive = true)
        goalRepository.updateGoal(inactiveGoal)
    }
    fun searchGoals(userId: Int, query: String) =
        goalRepository.searchGoals(userId, query)

    suspend fun completeGoalIfReached( //returns boolean if a goal has completed in a time window/frame and updates goal depending on it
        goalWithSessions: GoalWithSessions,
        now: ZonedDateTime
    ) :Boolean {
        // skip if time window was already completed
        if (goalWithSessions.hasCompletedCurrentWindow(now)) return false

        // if goals hasn't been reached skip
        if (!goalWithSessions.isCurrentlyAtOrAboveTarget(now)) return false

        // goal reached, set last completed at
        val updatedGoal = goalWithSessions.goal.copy(
            lastCompletedAt = timeProvider.now(),
            isCompleted = true
        )

        goalRepository.updateGoal(updatedGoal)
        return true
    }

    suspend fun resetCompletedGoalsIfNewCycle(userId: Int, now: ZonedDateTime) { //resets isCompleted for goals that have completed a cycle/time window but haven't completed the current one yet (e.g. daily goal that was completed yesterday but not today)
        val goals = goalRepository.getGoals(userId).firstOrNull() ?: return

        goals.forEach { goalWithSessions ->
            if (goalWithSessions.goal.isCompleted && !goalWithSessions.hasCompletedCurrentWindow(now)) {
                //new cycle/time window -> reset isCompleted
                val reset = goalWithSessions.goal.copy(isCompleted = false)
                goalRepository.updateGoal(reset)
            }
        }
    }
}