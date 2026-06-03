package com.example.purrsistence.domain.model

import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.ui.util.TimeWindow
import com.example.purrsistence.ui.util.currentDayWindow
import com.example.purrsistence.ui.util.currentMonthWindow
import com.example.purrsistence.ui.util.currentWeekWindow
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class GoalWithSessions(
    val goal: Goal,
    val sessions: List<TrackingSession>
) {
    fun totalTrackedDuration(now: Instant = Instant.now()): Duration {
        return calculateMergedDuration(null, now)
    }

    fun trackedDurationInWindow(window: TimeWindow, now: Instant = Instant.now()): Duration{ // Calculate the total tracked duration for sessions that started within the given time window
        return calculateMergedDuration(window, now)
    }

    private fun calculateMergedDuration(window: TimeWindow?, now: Instant): Duration {
        // Collect all working intervals from all sessions
        val allWorkingIntervals = sessions.flatMap { it.getWorkingIntervals(now) }

        // Clip to window if provided
        val clippedIntervals = if (window != null) {
            allWorkingIntervals.mapNotNull { (start, end) ->
                val clippedStart = if (start.isBefore(window.start)) window.start else start
                val clippedEnd = if (end.isAfter(window.end)) window.end else end

                if (clippedStart.isBefore(clippedEnd)) clippedStart to clippedEnd else null
            }
        } else {
            allWorkingIntervals
        }

        if (clippedIntervals.isEmpty()) return Duration.ZERO

        // Merge overlapping intervals
        val sortedIntervals = clippedIntervals.sortedBy { it.first }
        val mergedIntervals = mutableListOf<Pair<Instant, Instant>>()

        if (sortedIntervals.isNotEmpty()) {
            var currentMerged = sortedIntervals[0]
            for (i in 1 until sortedIntervals.size) {
                val next = sortedIntervals[i]
                if (next.first.isBefore(currentMerged.second) || next.first == currentMerged.second) {
                    // Overlap or contiguous, extend current merged interval
                    if (next.second.isAfter(currentMerged.second)) {
                        currentMerged = currentMerged.first to next.second
                    }
                } else {
                    // No overlap, push current and start new one
                    mergedIntervals.add(currentMerged)
                    currentMerged = next
                }
            }
            mergedIntervals.add(currentMerged)
        }

        return mergedIntervals.fold(Duration.ZERO) { acc, (start, end) ->
            acc.plus(Duration.between(start, end))
        }
    }

    fun currentProgress(now: ZonedDateTime): Float { // Calculate the progress towards the goal based on the tracked duration in the current time window (day, week, or month)
        val window = when (goal.type) {
            GoalType.DAILY -> currentDayWindow(now)
            GoalType.WEEKLY -> currentWeekWindow(now)
            GoalType.MONTHLY -> currentMonthWindow(now)
        }

        val tracked = trackedDurationInWindow(window, now.toInstant())
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