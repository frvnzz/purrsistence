package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import java.time.Duration

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
            isCompleted = isCompleted
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
        goalRepository.deleteGoal(goalId)
    }

    fun searchGoals(userId: Int, query: String) =
        goalRepository.searchGoals(userId, query)
}