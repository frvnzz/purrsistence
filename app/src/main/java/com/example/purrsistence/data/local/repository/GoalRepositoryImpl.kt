package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.GoalsDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalWithSessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface GoalRepository {
    fun getGoals(userId: Int): Flow<List<GoalWithSessions>>
    fun getActiveGoals(userId: Int): Flow<List<GoalWithSessions>>
    suspend fun insertGoal(goal: Goal)
    suspend fun deleteGoal(goalId: Int)
    fun getGoal(goalId: Int?): Flow<Goal?>
    fun getGoalWithSessions(goalId: Int?): Flow<GoalWithSessions?>
    suspend fun updateGoal(goal: Goal)
    fun searchGoals(userId: Int, query: String): Flow<List<GoalWithSessions>>
    suspend fun getInactiveGoals(): List<Goal>
    suspend fun getGoalsForSync(userId: Int): List<Goal>
    suspend fun replaceGoalsFromRemoteSync(
        userId: Int,
        goals: List<Goal>
    )
}

class GoalRepositoryImpl(
    private val dao: GoalsDao
) : GoalRepository {

    override fun getGoals(userId: Int): Flow<List<GoalWithSessions>> {
        return dao.getGoals(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActiveGoals(userId: Int): Flow<List<GoalWithSessions>> {
        return dao.getActiveGoals(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertGoal(goal: Goal) {
        dao.insertGoal(goal.toEntity())
    }

    override suspend fun deleteGoal(goalId: Int) {
        dao.deleteGoal(goalId)
    }

    override fun getGoal(goalId: Int?): Flow<Goal?> {
        return if (goalId == null) {
            flowOf(null)
        } else {
            dao.getGoal(goalId).map { entity ->
                entity?.toDomain()
            }
        }
    }

    override fun getGoalWithSessions(goalId: Int?): Flow<GoalWithSessions?> {
        return if (goalId == null) {
            flowOf(null)
        } else {
            dao.getGoalWithSessions(goalId).map { entity ->
                entity?.toDomain()
            }
        }
    }

    override suspend fun updateGoal(goal: Goal) {
        dao.updateGoal(
            goalId = goal.id,
            title = goal.title,
            type = goal.type.name,
            hours = goal.targetDuration.toMinutes().toInt(),
            deepFocus = goal.deepFocus,
            lastCompletedAt = goal.lastCompletedAt?.toEpochMilli(), //for updating the last completed time when marking as completed
            isCompleted = goal.isCompleted, //for showing the completed status in the UI
            inactive = goal.inactive
        )
    }

    override fun searchGoals(userId: Int, query: String): Flow<List<GoalWithSessions>> {
        return dao.searchGoalsWithSessions(userId, query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getInactiveGoals(): List<Goal> {
        return dao.getInactiveGoals().map { it.toDomain() }
    }

    override suspend fun getGoalsForSync(userId: Int): List<Goal> {
        return dao
            .getGoalEntitiesForUser(userId)
            .map { it.toDomain() }
    }

    override suspend fun replaceGoalsFromRemoteSync(
        userId: Int,
        goals: List<Goal>
    ) {
        dao.replaceGoalsForUser(
            userId = userId,
            goals = goals.map { goal ->
                goal.copy(userId = userId).toEntity()
            }
        )
    }

}

