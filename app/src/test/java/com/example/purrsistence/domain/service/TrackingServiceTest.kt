package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeTrackingRepository
import com.example.purrsistence.data.local.repository.FakeUserRepository
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.TrackingServiceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import kotlin.collections.listOf

class TrackingServiceTest {

    @Test
    fun startTracking_createsSessionWithCurrentTime() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
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
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds =  emptyList()
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(901000L))

        val result = service.stopTracking(session.id)

        assertNotNull(result)
        assertEquals(17, result!!.rewardedCurrency)
        assertEquals(1.15, result.multiplier, 0.0001)
        assertEquals(900000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1)
        assertEquals(17, updatedUser.first()?.balance)
    }

    @Test
    fun stopTracking_returnsNull_whenSessionDoesNotExist() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
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
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 50,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds =  emptyList()
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
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
    fun stopTracking_shortSessionBelowThreshold_usesBaseMultiplier() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds =  emptyList()
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(841_000L)) // 14 minutes after 1_000

        val result = service.stopTracking(session.id)

        assertEquals(14, result!!.rewardedCurrency)
        assertEquals(1.0, result.multiplier, 0.0001)
        assertEquals(840_000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(14, updatedUser!!.balance)
    }

    @Test
    fun stopTracking_capsRewardMultiplierAtTwoTimes() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds =  emptyList()
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(9_001_000L)) // 150 minutes

        val result = service.stopTracking(session.id)

        assertEquals(300, result!!.rewardedCurrency)
        assertEquals(2.0, result.multiplier, 0.0001)
        assertEquals(9_000_000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(300, updatedUser!!.balance)
    }
}