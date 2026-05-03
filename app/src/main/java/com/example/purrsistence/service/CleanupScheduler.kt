package com.example.purrsistence.service

import android.content.SharedPreferences
import com.example.purrsistence.domain.time.TimeProvider
import androidx.core.content.edit

class CleanupScheduler(
    private val sharedPreferences: SharedPreferences,
    private val timeProvider: TimeProvider,
    private val trackingCleanupService: TrackingCleanupService
) {
    suspend fun runIfDue() {
        val now = timeProvider.now().toEpochMilli()
        val lastRun = sharedPreferences.getLong("last_cleanup_timestamp", 0L)
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (now - lastRun >= oneDayMillis) {
            trackingCleanupService.cleanupInactiveGoalsAndOldSessions()
            sharedPreferences.edit { putLong("last_cleanup_timestamp", now) }
        }
    }
}