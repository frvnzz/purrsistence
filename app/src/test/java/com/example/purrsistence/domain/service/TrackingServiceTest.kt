package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeGoalRepository
import com.example.purrsistence.data.local.repository.FakeTrackingRepository
import com.example.purrsistence.data.local.repository.FakeUserRepository
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.GoalService
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.TrackingServiceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.URL
import java.time.Duration
import java.time.Instant

class TrackingServiceTest {

    @Test
    fun startTracking_createsSessionWithCurrentTime() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )

        assertEquals(7, session.goalId)
        assertEquals(1, session.userId)
        assertEquals(Instant.ofEpochMilli(1000L), session.startTime)
        assertNotNull(session.id)
    }

    @Test
    fun stopTracking_finishesSession_andAddsCurrency() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList(),
                profileImageUrl = URL("https://example.com/profile.png"),
                isSupabaseLinked = true,
                supabaseUserId = "supabase-123",
                localUpdatedAt = null,
                lastSyncedAt = null,
                hasPendingLocalChanges = false
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(901_000L))

        val result = service.stopTracking(session.id)

        assertNotNull(result)

        // 900_000 ms = 15 minutes = 30 reward intervals
        // multiplier capped at 2.0
        // 30 * 2.0 = 60
        assertEquals(60, result!!.rewardedCurrency)
        assertEquals(2.0, result.multiplier, 0.0001)
        assertEquals(900_000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1)
        assertEquals(60, updatedUser.first()?.balance)
    }

    @Test
    fun stopTracking_returnsNull_whenSessionDoesNotExist() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val result = service.stopTracking(999)

        assertNull(result)
    }

    @Test
    fun stopTracking_zeroDuration_givesZeroCoins_andKeepsBalance() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 50,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds =  emptyList(),
                profileImageUrl = URL("https://example.com/profile.png"),
                isSupabaseLinked = true,
                supabaseUserId = "supabase-123",
                localUpdatedAt = null,
                lastSyncedAt = null,
                hasPendingLocalChanges = false
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(1_000L))

        val result = service.stopTracking(session.id)

        assertEquals(0, result!!.rewardedCurrency)
        assertEquals(1.0, result.multiplier, 0.0001)
        assertEquals(0L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(50, updatedUser!!.balance)
    }

    @Test
    fun stopTracking_sessionBelowRewardInterval_givesZeroCoins() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList(),
                profileImageUrl = URL("https://example.com/profile.png"),
                isSupabaseLinked = true,
                supabaseUserId = "supabase-123",
                localUpdatedAt = null,
                lastSyncedAt = null,
                hasPendingLocalChanges = false
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )

        // Start was at 1_000L, so this is 29 seconds of effective tracking.
        timeProvider.setNow(Instant.ofEpochMilli(30_000L))

        val result = service.stopTracking(session.id)

        assertNotNull(result)
        assertEquals(0, result!!.rewardedCurrency)
        assertEquals(1.0, result.multiplier, 0.0001)
        assertEquals(29_000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(0, updatedUser!!.balance)
    }

    @Test
    fun stopTracking_capsRewardMultiplierAtTwoTimes() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList(),
                profileImageUrl = URL("https://example.com/profile.png"),
                isSupabaseLinked = true,
                supabaseUserId = "supabase-123",
                localUpdatedAt = null,
                lastSyncedAt = null,
                hasPendingLocalChanges = false
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(9_001_000L))

        val result = service.stopTracking(session.id)

        assertNotNull(result)

        // 150 minutes = 300 reward intervals
        // multiplier capped at 2.0
        // 300 * 2.0 = 600
        assertEquals(600, result!!.rewardedCurrency)
        assertEquals(2.0, result.multiplier, 0.0001)
        assertEquals(9_000_000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(600, updatedUser!!.balance)
    }

    @Test
    fun pauseTracking_updatesSessionWithPauseStartTime() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )
        timeProvider.setNow(Instant.ofEpochMilli(2000L))

        val success = service.pauseTracking(session.id)

        val updated = trackingRepository.getTrackingSessionById(session.id)
        assertEquals(true, success)
        assertEquals(Instant.ofEpochMilli(2000L), updated?.currentPauseStart)
    }

    @Test
    fun resumeTracking_calculatesPausedDurationAndClearsPauseStart() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )
        timeProvider.setNow(Instant.ofEpochMilli(2000L))
        service.pauseTracking(session.id)

        timeProvider.setNow(Instant.ofEpochMilli(5000L))
        val success = service.resumeTracking(session.id)

        val updated = trackingRepository.getTrackingSessionById(session.id)
        assertEquals(true, success)
        assertEquals(3000L, updated?.getTotalPausedMillis(timeProvider.now()))
        assertNull(updated?.currentPauseStart)
    }

    @Test
    fun stopTracking_withLongPause_resetsMultiplier() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val goalRepository = FakeGoalRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(0L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "User",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList(),
                profileImageUrl = URL("https://example.com/p.png"),
                isSupabaseLinked = false,
                supabaseUserId = null,
                localUpdatedAt = null,
                lastSyncedAt = null,
                hasPendingLocalChanges = false
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            goalRepository = goalRepository,
            goalService = GoalService(goalRepository, timeProvider),
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false,
            deepFocus = false
        )

        // Track 20 minutes.
        timeProvider.setNow(Instant.ofEpochMilli(Duration.ofMinutes(20).toMillis()))

        // Pause 16 minutes, which is over the 15-minute reset threshold.
        service.pauseTracking(session.id)
        timeProvider.setNow(Instant.ofEpochMilli(Duration.ofMinutes(36).toMillis()))
        service.resumeTracking(session.id)

        val result = service.stopTracking(session.id)

        assertNotNull(result)

        // The 20 minutes before the long pause are checkpointed.
        // 20 minutes = 40 reward intervals
        // multiplier capped at 2.0
        // 40 * 2.0 = 80
        assertEquals(80, result!!.rewardedCurrency)

        // After the long pause, the current block multiplier is reset.
        assertEquals(1.0, result.multiplier, 0.0001)

        // Reset was already handled during resumeTracking().
        assertEquals(false, result.multiplierReset)
        assertEquals(Duration.ofMinutes(16).toMillis(), result.totalPausedMillis)

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(80, updatedUser!!.balance)
    }

}