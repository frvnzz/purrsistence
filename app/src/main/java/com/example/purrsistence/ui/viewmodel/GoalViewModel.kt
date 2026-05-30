package com.example.purrsistence.ui.viewmodel

import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.service.GoalService
import com.example.purrsistence.service.TrackingSyncService
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class GoalViewModel(
    private val goalService: GoalService,
    private val sharedPreferences: SharedPreferences,
    private val supabaseSyncService: TrackingSyncService
) : ViewModel() {

    // Goal Selection state (HomeScreen goal picker)
    var selectedGoalId by mutableStateOf<Int?>(null)
        private set

    var searchQuery by mutableStateOf("")
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
        goalService.getGoals(userId)

    fun addGoal(
        userId: Int,
        title: String,
        type: String,
        weeklyMinutes: Int,
        deepFocus: Boolean,
        inactive: Boolean,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            goalService.createGoal(
                userId = userId,
                title = title,
                type = type,
                targetMinutes = weeklyMinutes,
                deepFocus = deepFocus,
                inactive = inactive,
                isCompleted = isCompleted
            )

            supabaseSyncService.syncAfterLocalGoalChanged()
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            goalService.deleteGoal(goalId)
            supabaseSyncService.syncAfterLocalGoalChanged()
        }
    }

    fun getGoal(goalId: Int?) =
        goalService.getGoal(goalId)

    fun getGoalWithSessions(goalId: Int?) =
        goalService.getGoalWithSessions(goalId)

    fun updateGoal(
        goalId: Int,
        title: String,
        type: GoalType,
        hours: Int,
        deepFocus: Boolean
    ) {
        viewModelScope.launch {
            goalService.updateGoal(goalId, title, type, hours, deepFocus)

            supabaseSyncService.syncAfterLocalGoalChanged()
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun searchedGoals(userId: Int) =
        goalService.searchGoals(userId, searchQuery)

    fun resetCompletedGoalsIfNewCycle(userId: Int) { //called on goal screen open to reset completed goals if a new time window has started
        viewModelScope.launch {
            goalService.resetCompletedGoalsIfNewCycle(userId, ZonedDateTime.now())
            supabaseSyncService.syncAfterLocalGoalChanged()
        }
    }
}