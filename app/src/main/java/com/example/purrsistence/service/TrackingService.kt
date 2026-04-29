package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.TrackingStopResult
import com.example.purrsistence.domain.time.TimeProvider
import java.time.Duration
import java.time.Instant

interface TrackingService{
    suspend fun startTracking(goalId: Int, userId: Int, pauseReminder: Boolean = false, deepFocus: Boolean = false): TrackingSession
    suspend fun stopTracking(trackingId: Int): TrackingStopResult?
}

class TrackingServiceImpl(
    private val trackingRepository: TrackingRepository,
    private val userRepository: UserRepository,
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

        return TrackingStopResult(
            rewardedCurrency = coins,
            multiplier = multiplier,
            sessionDurationMillis = sessionDurationMillis
        )
    }
}