package com.example.purrsistence.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalCrudIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: Dao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = db.dao()
    }

    @After
    fun clean() {
        db.close()
    }

    private suspend fun insertTestUser(userId: Int = 1) {
        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )
    }

    private fun createGoal(
        goalId: Int = 0,
        userId: Int = 1,
        title: String = "Test Goal",
        type: String = "Weekly",
        targetDuration: Int = 120,
        deepFocus: Boolean = true,
        inactive: Boolean = false,
        createdAt: Long = 1000L,
        isCompleted: Boolean = false
    ): GoalEntity {
        return GoalEntity(
            goalId = goalId,
            userId = userId,
            title = title,
            type = type,
            targetDuration = targetDuration,
            deepFocus = deepFocus,
            inactive = inactive,
            createdAt = createdAt,
            isCompleted = isCompleted
        )
    }

    @Test
    fun createGoal_insertsGoal_andAppearsInGoalList() = runBlocking {
        insertTestUser(1)

        dao.insertGoal(
            createGoal(
                userId = 1,
                title = "Read papers",
                type = "Weekly",
                targetDuration = 180,
                deepFocus = true
            )
        )

        val goals = dao.getGoals(1).first()

        assertEquals(1, goals.size)
        assertEquals("Read papers", goals[0].goal.title)
        assertEquals("Weekly", goals[0].goal.type)
        assertEquals(180, goals[0].goal.targetDuration)
        assertEquals(true, goals[0].goal.deepFocus)
    }

    @Test
    fun readGoal_returnsInsertedGoal() = runBlocking {
        insertTestUser(1)

        dao.insertGoal(
            createGoal(
                userId = 1,
                title = "Write thesis",
                type = "Monthly",
                targetDuration = 300,
                deepFocus = false
            )
        )

        val insertedGoal = dao.getGoals(1).first().first().goal
        val loadedGoal = dao.getGoal(insertedGoal.goalId).first()

        assertNotNull(loadedGoal)
        assertEquals(insertedGoal.goalId, loadedGoal!!.goalId)
        assertEquals("Write thesis", loadedGoal.title)
        assertEquals("Monthly", loadedGoal.type)
        assertEquals(300, loadedGoal.targetDuration)
        assertEquals(false, loadedGoal.deepFocus)
    }

    @Test
    fun updateGoal_updatesStoredValues() = runBlocking {
        insertTestUser(1)

        dao.insertGoal(
            createGoal(
                userId = 1,
                title = "Old Title",
                type = "Weekly",
                targetDuration = 60,
                deepFocus = true
            )
        )

        val insertedGoal = dao.getGoals(1).first().first().goal

        dao.updateGoal(
            goalId = insertedGoal.goalId,
            title = "New Title",
            type = "Weekly",
            hours = 90,
            deepFocus = true
        )

        val updatedGoal = dao.getGoal(insertedGoal.goalId).first()

        assertNotNull(updatedGoal)
        assertEquals("New Title", updatedGoal!!.title)
        assertEquals(90, updatedGoal.targetDuration)

        // These remain unchanged because the CURRENT Dao.updateGoal() only updates title and targetDuration.
        //TODO check if intended
        assertEquals("Weekly", updatedGoal.type)
        assertEquals(true, updatedGoal.deepFocus)
    }

    @Test
    fun deleteGoal_removesGoalFromDatabase() = runBlocking {
        insertTestUser(1)

        dao.insertGoal(
            createGoal(
                userId = 1,
                title = "Temporary Goal"
            )
        )

        val insertedGoal = dao.getGoals(1).first().first().goal

        dao.deleteGoal(insertedGoal.goalId)

        val goalAfterDelete = dao.getGoal(insertedGoal.goalId).first()
        val goalsAfterDelete = dao.getGoals(1).first()

        assertNull(goalAfterDelete)
        assertTrue(goalsAfterDelete.isEmpty())
    }
}