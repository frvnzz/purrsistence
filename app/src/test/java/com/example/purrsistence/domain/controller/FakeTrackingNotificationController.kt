package com.example.purrsistence.domain.controller


import com.example.purrsistence.controller.TrackingNotificationController

class FakeTrackingNotificationController : TrackingNotificationController {

    var startCalls = 0
    var stopCalls = 0
    var updateCalls = 0

    var lastTrackingId: Int? = null
    var lastGoalTitle: String? = null
    var lastStartTimeMillis: Long? = null
    var lastIsPaused: Boolean? = null
    var lastBaseTimeMillis: Long? = null

    override fun startTrackingNotification(
        trackingId: Int,
        goalTitle: String,
        startTimeMillis: Long
    ) {
        startCalls++
        lastTrackingId = trackingId
        lastGoalTitle = goalTitle
        lastStartTimeMillis = startTimeMillis
    }

    override fun updateTrackingNotification(
        trackingId: Int,
        goalTitle: String,
        isPaused: Boolean,
        startTimeMillis: Long,
        elapsedMillis: Long
    ) {
        updateCalls++
        lastTrackingId = trackingId
        lastGoalTitle = goalTitle
        lastIsPaused = isPaused
        lastStartTimeMillis = startTimeMillis
        lastBaseTimeMillis = System.currentTimeMillis() - elapsedMillis
    }

    override fun stopTrackingNotification() {
        stopCalls++
    }
}