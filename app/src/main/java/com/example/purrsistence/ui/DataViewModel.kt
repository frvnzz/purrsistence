package com.example.purrsistence.ui

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.DataRepository
import kotlinx.coroutines.launch
import androidx.core.content.edit

class DataViewModel(
    private val repository: DataRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // Goal Selection state (HomeScreen goal picker)
    var selectedGoalId by mutableStateOf<Int?>(null)
        private set

    init {
        // Load the saved goal ID on startup
        val savedId = sharedPreferences.getInt("selected_goal_id", -1)
        if (savedId != -1) {
            selectedGoalId = savedId
        }
    }

    fun selectGoal(id: Int) {
        selectedGoalId = id
        // Save the selection to disk
        sharedPreferences.edit { putInt("selected_goal_id", id) }
    }

    // CRUD GOAL

    fun goals(userId: Int) =
        repository.getGoals(userId)

    fun addGoal(
        userId: Int, // unnecessary danger of mismatch?
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

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun getGoal(goalId: Int?) =
        repository.getGoal(goalId)

    fun updateGoal(
        goalId: Int,
        title: String,
        type: String,
        hours: Int,
        deepFocus: Boolean
    ) {
        viewModelScope.launch {
            repository.updateGoal(goalId, title, type, hours, deepFocus)
        }
    }
}