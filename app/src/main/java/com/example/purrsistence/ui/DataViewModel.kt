package com.example.purrsistence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.DataRepository
import kotlinx.coroutines.launch

class DataViewModel(
    private val repository: DataRepository
) : ViewModel() {

    // Goal

    fun goals(userId: String) =
        repository.getGoals(userId)

    fun addGoal(
        userId: Int,
        title: String,
        type: String,
        weeklyMinutes: Int,
        deepFocus: Boolean,
        inactive: Boolean,
        createdAt: Long,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            repository.createGoal(
                userId = userId,
                title = title,
                type = type,
                weeklyMinutes = weeklyMinutes,
                deepFocus = deepFocus,
                inactive = inactive,
                createdAt = createdAt,
                isCompleted = isCompleted
            )
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun getGoal(goalId: Long?) =
        repository.getGoal(goalId)

    fun updateGoal(goalId: Long, title: String, hours: Int) {
        viewModelScope.launch {
            repository.updateGoal(goalId, title, hours)
        }
    }
}