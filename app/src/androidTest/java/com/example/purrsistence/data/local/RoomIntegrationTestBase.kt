package com.example.purrsistence.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.purrsistence.data.local.dao.GoalsDao
import com.example.purrsistence.data.local.dao.TrackingDao
import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.GoalRepositoryImpl
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.data.local.repository.UserRepositoryImpl
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

abstract class RoomIntegrationTestBase {

    protected lateinit var db: AppDatabase

    protected lateinit var userDao: UserDao
    protected lateinit var goalsDao: GoalsDao
    protected lateinit var trackingDao: TrackingDao

    protected lateinit var userRepository: UserRepository
    protected lateinit var goalRepository: GoalRepository
    protected lateinit var trackingRepository: TrackingRepository
    protected val timeProvider : TimeProvider = FakeTimeProvider()

    @Before
    fun setupDatabase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        userDao = db.userDao()
        goalsDao = db.goalsDao()
        trackingDao = db.trackingDao()

        userRepository = UserRepositoryImpl(userDao, timeProvider = timeProvider )
        goalRepository = GoalRepositoryImpl(goalsDao)
        trackingRepository = TrackingRepositoryImpl(trackingDao)
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    protected fun seedUserEntity(
        userId: Int = 1,
        username: String = "TestUser",
        balance: Int = 0,
        friends: List<String> = emptyList(),
        collectedCatsIds: List<String> = emptyList(),
        selectedCatIds: List<String> = emptyList(),
        profileImageUrl: String? = null,
        isSupabaseLinked: Boolean = false,
        supabaseUserId: String? = null
    ) = runBlocking {
        userDao.insertUser(
            UserEntity(
                userId = userId,
                username = username,
                balance = balance,
                friends = friends,
                collectedCatsIds = collectedCatsIds,
                selectedCatIds = selectedCatIds,
                profileImageUrl = profileImageUrl,
                isSupabaseLinked = isSupabaseLinked,
                supabaseUserId = supabaseUserId
            )
        )
    }

    protected fun seedGoalEntity(
        goalId: Int = 1,
        userId: Int = 1,
        title: String = "Goal",
        type: String = "WEEKLY",
        targetDuration: Int = 120,
        deepFocus: Boolean = false,
        inactive: Boolean = false,
        createdAt: Long = 1_700_000_000_000L,
        isCompleted: Boolean = false,
        lastCompletedAt: Long? = null
    ) = runBlocking {
        goalsDao.insertGoal(
            GoalEntity(
                goalId = goalId,
                userId = userId,
                title = title,
                type = type,
                targetDuration = targetDuration,
                deepFocus = deepFocus,
                inactive = inactive,
                createdAt = createdAt,
                isCompleted = isCompleted,
                lastCompletedAt = lastCompletedAt
            )
        )
    }

    protected fun seedTrackingSessionEntity(
        sessionId: Int = 0,
        goalId: Int,
        userId: Int,
        startTime: Long = 1_000_000L,
        duration: Long = 1_000L
    ) = runBlocking {
        trackingDao.insertTrackingSession(
            TrackingSessionEntity(
                trackingId = sessionId,
                goalId = goalId,
                userId = userId,
                pauseReminder = false,
                deepFocus = false,
                startTime = startTime,
                endTime = startTime + duration
            )
        )
    }
}
