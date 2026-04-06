package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.Goal
import kotlinx.coroutines.flow.flowOf

class DataRepository (
    private val dao: Dao
) {

    fun getGoals(userId: Int) =
        dao.getGoals(userId)

    suspend fun createGoal(
        userId: Int,
        title: String,
        type: String,
        weeklyMinutes: Int,
        deepFocus: Boolean,
        inactive: Boolean,
        createdAt: Long,
        isCompleted: Boolean
    ) {
        dao.insertGoal(
            Goal(
                userId = userId,
                title = title,
                type = type,
                targetDuration = weeklyMinutes,
                deepFocus = deepFocus,
                inactive = inactive,
                createdAt = createdAt,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun deleteGoal(goalId: Int) {
        dao.deleteGoal(goalId)
    }

    fun getGoal(goalId: Int?) =
        if (goalId == null) flowOf(null)
        else dao.getGoal(goalId)

    suspend fun updateGoal(
        goalId: Int,
        title: String,
        type: String,
        hours: Int,
        deepFocus: Boolean
    ) {
        dao.updateGoal(goalId, title, type, hours, deepFocus)
    }
}
