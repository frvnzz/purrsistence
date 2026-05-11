package com.example.purrsistence.ui.navigation

sealed interface GoalEvent {
    data class GoalCompleted(
        val goalId: Int,
        val rewardAmount: Int,
        val goalTitle: String,
    ): GoalEvent
}