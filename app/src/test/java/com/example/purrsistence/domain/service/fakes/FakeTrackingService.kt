package com.example.purrsistence.domain.service.fakes

import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.TrackingStopResult
import com.example.purrsistence.service.TrackingService
import java.time.Instant

class FakeTrackingService : TrackingService {

    var startCalls = 0
    var stopCalls = 0

    var lastStartedGoalId: Int? = null
    var lastStartedUserId: Int? = null

    val stoppedTrackingIds = mutableListOf<Int>()

    override suspend fun startTracking(
        goalId: Int,
        userId: Int,
        pauseReminder: Boolean,
        deepFocus: Boolean
    ): TrackingSession {
        startCalls++
        lastStartedGoalId = goalId
        lastStartedUserId = userId

        return TrackingSession(
            id = 1,
            goalId = goalId,
            userId = userId,
            pauseReminder = pauseReminder,
            deepFocus = true,
            startTime = Instant.ofEpochMilli(1_000L),
            endTime = null
        )
    }

    override suspend fun stopTracking(trackingId: Int): TrackingStopResult {
        stopCalls++
        stoppedTrackingIds += trackingId

        return TrackingStopResult(
            rewardedCurrency = 10,
            multiplier = 1.0,
            sessionDurationMillis = 60_000L
        )
    }
}