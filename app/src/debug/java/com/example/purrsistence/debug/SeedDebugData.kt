package com.example.purrsistence.debug

import android.content.Context
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Debug-only helper to seed the app Room database with example goals and tracking sessions.
 *
 * Put this file under `app/src/debug/java/...` so it is only compiled into debug builds.
 * Usage (example):
 *
 * lifecycleScope.launch {
 *   SeedDebugData.seedDebugData(this@MainActivity)
 * }
 */
object SeedDebugData {

    /**
     * Insert a small, fixed dataset for manual testing / debugging.
     * This is safe to call multiple times (it will attempt inserts with explicit ids).
     */
    suspend fun seedDebugData(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val userDao = db.userDao()
        val goalsDao = db.goalsDao()
        val trackingDao = db.trackingDao()

        // Ensure a test user (id = 1) exists. We provide explicit id so foreign keys can point to it.
        try {
            userDao.insertUser(
                UserEntity(
                    userId = 1,
                    username = "DebugUser",
                    profileImageUrl = null,
                    balance = 500,
                    isSupabaseLinked = false,
                    supabaseUserId = null,
                    friends = emptyList(),
                    collectedCatsIds = emptyList(),
                    selectedCatIds = emptyList()
                )
            )
        } catch (_: Exception) {
            // ignore if already present
        }

        // Create several goals with different creation dates and human-friendly titles
        val goals = listOf(
            // older
            GoalEntity(
                goalId = 101,
                userId = 1,
                title = "Read War and Peace",
                type = "Monthly",
                targetDuration = 300, // minutes
                deepFocus = false,
                inactive = false,
                createdAt = 1622505600000L, // 2021-06-01
                isCompleted = false,
                lastCompletedAt = null
            ),

            GoalEntity(
                goalId = 102,
                userId = 1,
                title = "Daily Meditation",
                type = "Daily",
                targetDuration = 20,
                deepFocus = false,
                inactive = false,
                createdAt = 1654041600000L, // 2022-06-01
                isCompleted = false,
                lastCompletedAt = null
            ),

            GoalEntity(
                goalId = 103,
                userId = 1,
                title = "Weekly Exercise",
                type = "Weekly",
                targetDuration = 180,
                deepFocus = false,
                inactive = false,
                createdAt = 1685577600000L, // 2023-06-01
                isCompleted = false,
                lastCompletedAt = null
            ),

            GoalEntity(
                goalId = 104,
                userId = 1,
                title = "Monthly Budget Review",
                type = "Monthly",
                targetDuration = 60,
                deepFocus = false,
                inactive = false,
                createdAt = 1717113600000L, // 2024-06-01
                isCompleted = false,
                lastCompletedAt = null
            ),

            GoalEntity(
                goalId = 105,
                userId = 1,
                title = "Master Kotlin",
                type = "Weekly",
                targetDuration = 240,
                deepFocus = true,
                inactive = false,
                createdAt = 1748649600000L, // 2025-06-01
                isCompleted = false,
                lastCompletedAt = null
            ),

            GoalEntity(
                goalId = 106,
                userId = 1,
                title = "Deep Work Sessions",
                type = "Daily",
                targetDuration = 120,
                deepFocus = true,
                inactive = false,
                createdAt = 1778690872594L, // example timestamp provided
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        // Insert goals (ignore errors if already present)
        goals.forEach { g ->
            try {
                goalsDao.insertGoal(g)
            } catch (_: Exception) {
                // ignore duplicates or constraint errors
            }
        }

        // Create some tracking sessions for a few goals. Use millisecond timestamps.
        val sessions = listOf(
            // goal 101 had a completed session in 2021
            TrackingSessionEntity(
                trackingId = 0,
                goalId = 101,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = 1622592000000L, // 2021-06-02
                endTime = 1622595600000L // +1 hour
            ),

            // goal 102 recent daily meditation
            TrackingSessionEntity(
                trackingId = 0,
                goalId = 102,
                userId = 1,
                pauseReminder = false,
                deepFocus = false,
                startTime = 1778600000000L,
                endTime = 1778600120000L
            ),

            // goal 105 deep focus longer session
            TrackingSessionEntity(
                trackingId = 0,
                goalId = 105,
                userId = 1,
                pauseReminder = false,
                deepFocus = true,
                startTime = 1778680000000L,
                endTime = 1778683600000L
            ),

            // ongoing session for goal 106 (endTime = null)
            TrackingSessionEntity(
                trackingId = 0,
                goalId = 106,
                userId = 1,
                pauseReminder = false,
                deepFocus = true,
                startTime = 1778690000000L,
                endTime = null
            )
        )

        sessions.forEach { s ->
            try {
                trackingDao.insertTrackingSession(s)
            } catch (_: Exception) {
                // ignore duplicates
            }
        }
    }
}

