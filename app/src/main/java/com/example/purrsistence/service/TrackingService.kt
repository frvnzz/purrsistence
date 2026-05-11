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
        val finishedSession = trackingRepository.finishTrackingSession(
            trackingId = trackingId,
            endTimeMillis = timeProvider.now().toEpochMilli()
        ) ?: return null

        val duration = finishedSession.finishedDuration() ?: Duration.ZERO
        val sessionDurationMillis = duration.toMillis()

        val (coins, multiplier) = rewardService.calculateReward(duration)

        if (coins > 0) {
            userRepository.addCurrency(finishedSession.userId, coins)
        }

        //Check if goal has been reached after stopping tracking
        val goalsWithSessions = goalService.getGoals(finishedSession.userId).firstOrNull()
        val goalWithSessions = goalsWithSessions?.find { it.goal.id == finishedSession.goalId }

        var goalCompletionReward = 0
        goalWithSessions?.let {
            val wasCompleted = goalService.completeGoalIfReached(it, timeProvider.now().atZone(ZoneId.systemDefault()))
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
        )
    }

    private fun calculateGoalCompletionReward(goal: Goal): Int {
        return when (goal.type) {
            GoalType.DAILY -> 50
            GoalType.WEEKLY -> 200
            GoalType.MONTHLY -> 500
        }
    }
}