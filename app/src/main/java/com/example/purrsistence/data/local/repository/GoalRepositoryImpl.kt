package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.GoalsDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalWithSessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GoalRepository {
    fun getGoals(userId: Int): Flow<List<GoalWithSessions>>
    suspend fun insertGoal(goal: Goal)
    suspend fun deleteGoal(goalId: Int)
    fun getGoal(goalId: Int?): Flow<Goal?>
    suspend fun updateGoal(goal: Goal)
    fun searchGoals(userId: Int, query: String): Flow<List<GoalWithSessions>>
}

class GoalRepositoryImpl(
    private val dao: GoalsDao
) : GoalRepository {

    override fun getGoals(userId: Int): Flow<List<GoalWithSessions>> {
        return dao.getGoals(userId).map { list ->
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
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            dao.getGoal(goalId).map { entity ->
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
            deepFocus = goal.deepFocus
        )
    }

    override fun searchGoals(userId: Int, query: String): Flow<List<GoalWithSessions>> {
        return dao.searchGoalsWithSessions(userId, query).map { list ->
            list.map { it.toDomain() }
        }
    }
}

