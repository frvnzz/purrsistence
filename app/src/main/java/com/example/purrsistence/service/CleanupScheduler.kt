package com.example.purrsistence.service

import android.content.SharedPreferences
import com.example.purrsistence.domain.time.TimeProvider
import androidx.core.content.edit
import com.example.purrsistence.domain.preferences.CleanupPreferences

class CleanupScheduler(
    private val preferences: CleanupPreferences,
    private val timeProvider: TimeProvider,
    private val cleanupRunner: CleanupRunner
) {
    suspend fun runIfDue() {
        val now = timeProvider.now().toEpochMilli()
        val lastRun = preferences.getLastCleanupTimestamp()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (now - lastRun >= oneDayMillis) {
            cleanupRunner.runCleanup()
            preferences.setLastCleanupTimestamp(now)
        }
    }
}