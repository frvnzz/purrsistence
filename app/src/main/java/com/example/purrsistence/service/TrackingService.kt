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
import java.time.ZoneId
import kotlin.math.roundToInt

interface TrackingService{
    suspend fun getActiveTrackingSession(): TrackingSession?
    suspend fun getTrackingGoalTitle(goalId: Int): String
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

    // check if there is ANY currently active TrackingSession (for restoring purposes)
    override suspend fun getActiveTrackingSession(): TrackingSession? {
        return trackingRepository.getAnyActiveTrackingSession()
    }

    override suspend fun getTrackingGoalTitle(goalId: Int): String {
        return goalRepository
            .getGoal(goalId)
            .firstOrNull()
            ?.title
            ?: ""
    }

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
            endTime = null,
            pauseHistory = "0;;0" //start pause History with 0 pause time; no tracking intervals; 0 checkpointed currency
        )

        return trackingRepository.insertTrackingSession(session)
    }

    override suspend fun stopTracking(trackingId: Int): TrackingStopResult? {
        val now = timeProvider.now()
        val finishedSession = trackingRepository.finishTrackingSession(
            trackingId = trackingId,
            endTimeMillis = now.toEpochMilli()
        ) ?: return null

        val effectiveMillisSinceReset = finishedSession.getEffectiveMillisSinceLastReset(now)
        val hasLongPause = finishedSession.hasLongPause(now)
        val checkpointed = finishedSession.getCheckpointedCurrency()

        val (coins, multiplier) = rewardService.calculateReward(
            duration = Duration.ofMillis(effectiveMillisSinceReset.toLong()),
            hasLongPause = hasLongPause,
            checkpointedCurrency = checkpointed
        )

        if (coins > 0) {
            userRepository.addCurrency(finishedSession.userId, coins)
        }

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
            sessionDurationMillis = finishedSession.effectiveDuration(now).toMillis(),
            goalCompletionReward = goalCompletionReward,
            totalPausedMillis = finishedSession.getTotalPausedMillis(now),
            multiplierReset = hasLongPause
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
        return true
    }

    override suspend fun resumeTracking(trackingId: Int): Boolean {
        val session = trackingRepository.getTrackingSessionById(trackingId) ?: return false
        val pauseStart = session.currentPauseStart ?: return false //not currently paused
        val now = timeProvider.now()
        val pauseDuration = Duration.between(pauseStart, now).toMillis()
        
        val segments = session.pauseHistory.split(";")
        val baseTotal = segments.getOrNull(0)?.toLongOrNull() ?: 0L
        val intervals = segments.getOrNull(1) ?: ""
        var checkpointed = segments.getOrNull(2)?.toIntOrNull() ?: 0
        
        val newIntervals = if (intervals.isEmpty()) "${pauseStart.toEpochMilli()}-${now.toEpochMilli()}" else "$intervals,${pauseStart.toEpochMilli()}-${now.toEpochMilli()}"
        val newTotal = baseTotal + pauseDuration

        var lastReset = session.lastResetTime
        
        //Sectioned Reward: If this pause was > 15 min, checkpoint the coins/currency
        if (Duration.between(pauseStart, now).toMinutes() >= 15) {
            val millisBeforePause = session.getEffectiveMillisSinceLastReset(pauseStart)

            val (earnedCoins, _) = rewardService.calculateReward(
                duration = Duration.ofMillis(millisBeforePause.toLong()),
                hasLongPause = false,
                checkpointedCurrency = 0
            )

            checkpointed += earnedCoins
            lastReset = now
        }

        val updated = session.copy(
            pauseHistory = "$newTotal;$newIntervals;$checkpointed", //write pause history in string format
            currentPauseStart = null,
            lastResetTime = lastReset
        )
        trackingRepository.updateTrackingSession(updated)
        return true
    }
}