package com.example.purrsistence.data.local.repository

import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalWithSessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeGoalRepository : GoalRepository {

    private val goals = mutableListOf<Goal>()
    private val goalsFlow = MutableStateFlow<List<Goal>>(emptyList())
    private var nextId = 1

    override fun getGoals(userId: Int): Flow<List<GoalWithSessions>> {
        return goalsFlow.map { list ->
            list.filter { it.userId == userId }
                .map { GoalWithSessions(it, emptyList()) }
        }
    }

    override suspend fun insertGoal(goal: Goal) {
        val stored = goal.copy(id = nextId++)
        goals.add(stored)
        goalsFlow.value = goals.toList()
    }

    override suspend fun deleteGoal(goalId: Int) {
        goals.removeAll { it.id == goalId }
        goalsFlow.value = goals.toList()
    }

    override fun getGoal(goalId: Int?): Flow<Goal?> {
        return goalsFlow.map { list -> list.find { it.id == goalId } }
    }

    override suspend fun updateGoal(goal: Goal) {
        val index = goals.indexOfFirst { it.id == goal.id }
        if (index == -1) return
        goals[index] = goal
        goalsFlow.value = goals.toList()
    }

    override fun searchGoals(userId: Int, query: String): Flow<List<GoalWithSessions>> {
        return goalsFlow.map { list ->
            list.filter {
                it.userId == userId && it.title.contains(query, ignoreCase = true)
            }.map { GoalWithSessions(it, emptyList()) }
        }
    }

}