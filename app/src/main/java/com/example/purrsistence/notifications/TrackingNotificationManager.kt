package com.example.purrsistence.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class TrackingNotificationManager(
    private val context: Context
) {

    fun createChannels() {
        val channel = NotificationChannel(
            NotificationChannels.TRACKING,
            "Tracking session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the current tracking session timer"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}