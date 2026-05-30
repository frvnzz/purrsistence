package com.example.purrsistence.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class ReminderNotificationManager(
    private val context: Context
) {

    fun createChannels() {
        val reminderChannel = NotificationChannel(
            NotificationChannels.REMINDERS,
            "Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Gentle reminders to return and track sessions"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(reminderChannel)
    }
}