package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.flow.forEach
import java.time.Duration

class TrackingCleanupService(
    private val goalRepository: GoalRepository,
    private val trackingRepository: TrackingRepository,
    private val timeProvider: TimeProvider
) {

    private val retentionDuration: Duration = Duration.ofDays(28)

    suspend fun cleanupInactiveGoalsAndOldSessions() {
        val cutoff = timeProvider.now().minus(retentionDuration)

        val inactiveGoals = goalRepository.getInactiveGoals()

        inactiveGoals.forEach { goal ->
            trackingRepository.deleteFinishedSessionsForGoalBefore(
                goalId = goal.id,
                cutoff = cutoff
            )

            val remainingSessions = trackingRepository.countSessionsForGoal(goal.id)

            if (remainingSessions == 0) {
                goalRepository.deleteGoal(goal.id)
            }
        }
    }
}