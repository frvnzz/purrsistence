package com.example.purrsistence.domain.model

import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.ui.util.TimeWindow
import com.example.purrsistence.ui.util.currentDayWindow
import com.example.purrsistence.ui.util.currentMonthWindow
import com.example.purrsistence.ui.util.currentWeekWindow
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

data class GoalWithSessions(
    val goal: Goal,
    val sessions: List<TrackingSession>
) {
    fun totalTrackedDuration(): Duration {
        return sessions
            .mapNotNull { it.finishedDuration() }
            .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }
    }

    fun trackedDurationInWindow(window: TimeWindow): Duration{ // Calculate the total tracked duration for sessions that started within the given time window
        return sessions
            .filter { session ->
                val start = session.startTime
                start >= window.start && start < window.end
            }
            .fold(Duration.ZERO) { acc, session ->
                acc.plus(session.finishedDuration())
            }
    }

    fun currentProgress(now: ZonedDateTime): Float { // Calculate the progress towards the goal based on the tracked duration in the current time window (day, week, or month)
        val window = when (goal.type) {
            GoalType.DAILY -> currentDayWindow(now)
            GoalType.WEEKLY -> currentWeekWindow(now)
            GoalType.MONTHLY -> currentMonthWindow(now)
        }

        val tracked = trackedDurationInWindow(window)
        val target = goal.targetDuration

        return (tracked.toMillis().toFloat() / target.toMillis())
            .coerceIn(0f, 1f)
    }

    fun hasCompletedCurrentWindow(now: ZonedDateTime): Boolean { // Check if the goal has been completed in the current time window by comparing the last completed time with the start of the current window
        if(goal.lastCompletedAt == null) return false //never completed before

        val window = when (goal.type) {
            GoalType.DAILY -> currentDayWindow(now)
            GoalType.WEEKLY -> currentWeekWindow(now)
            GoalType.MONTHLY -> currentMonthWindow(now)
        }

        val completedTime = goal.lastCompletedAt.atZone(ZoneId.systemDefault())
        return completedTime.toInstant() >= window.start && completedTime.toInstant() < window.end
    }

    fun isCurrentlyAtOrAboveTarget(now: ZonedDateTime): Boolean {
        return currentProgress(now) >= 1.0f
    }

}