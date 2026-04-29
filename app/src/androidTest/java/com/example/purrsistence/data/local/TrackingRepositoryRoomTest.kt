package com.example.purrsistence.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.domain.time.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TrackingRepositoryRoomTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: Dao
    private lateinit var repositoryImpl: TrackingRepositoryImpl
    private lateinit var fakeTimeProvider: FakeTimeProvider

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = db.dao()
        fakeTimeProvider = FakeTimeProvider(1000L)
        repositoryImpl = TrackingRepositoryImpl(dao, fakeTimeProvider)

    }

    @After
    fun clean(){
        db.close()
    }

    @Test
    fun startAndStopTracking () = runBlocking {
        val testGoalId = 100

        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUsr",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )
        dao.insertGoal(
            GoalEntity(
                goalId = testGoalId,
                userId = 1,
                title = "Test Goal",
                type = "Focus",
                targetDuration = 25,
                deepFocus = false,
                inactive = false,
                createdAt = 1000L,
                isCompleted = false
            )
        )

        val started = repositoryImpl.startTracking(
            goalId = testGoalId,
            userId = 1,
            pauseReminder = false
        )

        fakeTimeProvider.currentTime = 7000L
        repositoryImpl.stopTracking(started.trackingId)

        val stored = repositoryImpl.getTrackingSessionById(started.trackingId)

        assertNotNull(stored)
        assertEquals(1000L, stored!!.startTime)
        assertEquals(7000L, stored.endTime)
    }
}