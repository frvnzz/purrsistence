package com.example.purrsistence.domain.controller


import com.example.purrsistence.controller.TrackingNotificationController

class FakeTrackingNotificationController : TrackingNotificationController {

    var startCalls = 0
    var stopCalls = 0

    var lastTrackingId: Int? = null
    var lastGoalTitle: String? = null
    var lastStartTimeMillis: Long? = null

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

    override fun stopTrackingNotification() {
        stopCalls++
    }
}