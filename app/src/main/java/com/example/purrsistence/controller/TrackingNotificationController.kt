package com.example.purrsistence.controller

import android.content.Context
import com.example.purrsistence.service.TrackingForegroundService

interface TrackingNotificationController {
    fun startTrackingNotification(
        trackingId: Int,
        goalTitle: String,
        startTimeMillis: Long
    )

    fun stopTrackingNotification()
}

class TrackingNotificationControllerImpl(
    private val appContext: Context
) : TrackingNotificationController {

    override fun startTrackingNotification(
        trackingId: Int,
        goalTitle: String,
        startTimeMillis: Long
    ) {
        TrackingForegroundService.start(
            context = appContext,
            trackingId = trackingId,
            goalTitle = goalTitle,
            startTimeMillis = startTimeMillis
        )
    }

    override fun stopTrackingNotification() {
        TrackingForegroundService.stop(appContext)
    }
}