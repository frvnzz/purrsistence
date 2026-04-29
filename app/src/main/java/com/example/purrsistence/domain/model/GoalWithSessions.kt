package com.example.purrsistence.domain.model

import java.time.Duration

data class GoalWithSessions(
    val goal: Goal,
    val sessions: List<TrackingSession>
) {
    fun totalTrackedDuration(): Duration {
        return sessions
            .mapNotNull { it.finishedDuration() }
            .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }
    }
}