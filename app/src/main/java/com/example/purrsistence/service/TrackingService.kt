package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.TrackingStopResult
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.flow.firstOrNull
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

interface TrackingService{
    suspend fun startTracking(goalId: Int, userId: Int, pauseReminder: Boolean = false, deepFocus: Boolean = false): TrackingSession
    suspend fun stopTracking(trackingId: Int): TrackingStopResult?
    suspend fun pauseTracking(trackingId: Int): Boolean
    suspend fun resumeTracking(trackingId: Int): Boolean
}

class TrackingServiceImpl(
    private val trackingRepository: TrackingRepository,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val goalService: GoalService,
    private val rewardService: RewardService,
    private val timeProvider: TimeProvider
) : TrackingService{

    override suspend fun startTracking(
        goalId: Int,
        userId: Int,
        pauseReminder: Boolean,
        deepFocus: Boolean
    ): TrackingSession {
        val session = TrackingSession(
            goalId = goalId,
            userId = userId,
            pauseReminder = pauseReminder,
            deepFocus = deepFocus,
            startTime = timeProvider.now(),
            endTime = null
        )

        return trackingRepository.insertTrackingSession(session)
    }

    override suspend fun stopTracking(trackingId: Int): TrackingStopResult? {
        val now = timeProvider.now()
        val finishedSession = trackingRepository.finishTrackingSession(
            trackingId = trackingId,
            endTimeMillis = now.toEpochMilli()
        ) ?: return null

        val totalDuration = finishedSession.duration(now)
        val sessionDurationMillis = totalDuration.toMillis()

        val effectiveDuration = finishedSession.finishedDuration() ?: Duration.ZERO
        val (coins, multiplier) = rewardService.calculateReward(
            duration = effectiveDuration,
            pausedMillis = finishedSession.pausedTimeMillis
        )

        if (coins > 0) {
            userRepository.addCurrency(finishedSession.userId, coins)
        }

        val multiplierReset = finishedSession.pausedTimeMillis > Duration.ofMinutes(15).toMillis()

        //Check if goal has been reached after stopping tracking
        val goalsWithSessions = goalService.getGoals(finishedSession.userId).firstOrNull()
        val goalWithSessions = goalsWithSessions?.find { it.goal.id == finishedSession.goalId }

        var goalCompletionReward = 0
        goalWithSessions?.let {
            val wasCompleted = goalService.completeGoalIfReached(it, now.atZone(ZoneId.systemDefault()))
            if(wasCompleted) {
                goalCompletionReward = calculateGoalCompletionReward(it.goal)
                userRepository.addCurrency(finishedSession.userId, goalCompletionReward)
            }
        }

        return TrackingStopResult(
            rewardedCurrency = coins,
            multiplier = multiplier,
            sessionDurationMillis = sessionDurationMillis,
            goalCompletionReward = goalCompletionReward,
            totalPausedMillis = finishedSession.pausedTimeMillis,
            multiplierReset = multiplierReset
        )
    }

    private fun calculateGoalCompletionReward(goal: Goal): Int {
        return when (goal.type) {
            GoalType.DAILY -> 50
            GoalType.WEEKLY -> 200
            GoalType.MONTHLY -> 500
        }
    }

    override suspend fun pauseTracking(trackingId: Int): Boolean {
        val session = trackingRepository.getTrackingSessionById(trackingId) ?: return false
        if (session.currentPauseStart != null) return false //already paused
        val now = timeProvider.now()
        val updated = session.copy(currentPauseStart = now)
        trackingRepository.updateTrackingSession(updated)
        println("Tracking session $trackingId paused at ${updated.currentPauseStart}")
        return true
    }

    override suspend fun resumeTracking(trackingId: Int): Boolean {
        val session = trackingRepository.getTrackingSessionById(trackingId) ?: return false
        val pauseStart = session.currentPauseStart ?: return false //not currently paused
        val now = timeProvider.now()
        val pauseDuration = Duration.between(pauseStart, now).toMillis()
        val newPausedTotal = session.pausedTimeMillis + pauseDuration
        val updated = session.copy(
            pausedTimeMillis = newPausedTotal,
            currentPauseStart = null,
        )
        trackingRepository.updateTrackingSession(updated)
        return true
    }
}