package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.relation.GoalWithSessionsEntity
import kotlinx.coroutines.flow.Flow

// TODO: SPLIT DAO

@Dao
interface GoalsDao {

    // GOAL
    @Insert
    suspend fun insertGoal(goalEntity: GoalEntity)

    @Transaction
    @Query("SELECT * FROM GoalEntity WHERE userId = :userId")
    fun getGoals(userId: Int): Flow<List<GoalWithSessionsEntity>>

    @Transaction
    @Query("SELECT * FROM GoalEntity WHERE userId = :userId AND inactive = 0")
    fun getActiveGoals(userId: Int): Flow<List<GoalWithSessionsEntity>>

    @Query("SELECT * FROM GoalEntity WHERE userId = :userId")
    fun getGoalsRaw(userId: Int): Flow<List<GoalEntity>> //get only the goals data

    @Query("DELETE FROM GoalEntity WHERE goalId = :goalId")
    suspend fun deleteGoal(goalId: Int)

    @Query("SELECT * FROM GoalEntity WHERE goalId = :goalId")
    fun getGoal(goalId: Int): Flow<GoalEntity?>

    @Transaction
    @Query("SELECT * FROM GoalEntity WHERE goalId = :goalId")
    fun getGoalWithSessions(goalId: Int): Flow<GoalWithSessionsEntity?>

    @Query(
        """
    UPDATE GoalEntity 
    SET title = :title,
        type = :type,
        targetDuration = :hours,
        deepFocus = :deepFocus,
        lastCompletedAt = :lastCompletedAt,
        inactive = :inactive,
        isCompleted = :isCompleted
    WHERE goalId = :goalId
    """
    )
    suspend fun updateGoal(
        goalId: Int,
        title: String,
        type: String,
        hours: Int,
        deepFocus: Boolean,
        lastCompletedAt: Long?,
        inactive: Boolean,
        isCompleted: Boolean
    )

    @Transaction
    @Query(
        """
    SELECT * FROM GoalEntity 
    WHERE userId = :userId 
    AND inactive = 0
    AND (:query = '' OR title LIKE '%' || :query || '%')
    """
    )
    fun searchGoalsWithSessions(
        userId: Int,
        query: String
    ): Flow<List<GoalWithSessionsEntity>>

    @Query(
        """    UPDATE GoalEntity     SET lastCompletedAt = :completedAt    WHERE goalId = :goalId    """
    )
    suspend fun updateLastCompletedAt(goalId: Int, completedAt: Long)
    @Query("SELECT * FROM GoalEntity WHERE inactive = 1")
    suspend fun getInactiveGoals(): List<GoalEntity>

    @Query(
        """
    UPDATE GoalEntity
    SET isCompleted = 0,
        lastCompletedAt = NULL
    WHERE userId = :userId
    """
    )
    suspend fun resetGoalsStatusForUser(userId: Int)

    @Query(
        """
    SELECT *
    FROM GoalEntity
    WHERE userId = :userId
    """
    )
    suspend fun getGoalEntitiesForUser(userId: Int): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoalEntities(
        goals: List<GoalEntity>
    )

    @Query(
        """
        DELETE FROM GoalEntity
        WHERE userId = :userId
        """
    )
    suspend fun deleteGoalsForUser(
        userId: Int
    )

    @Transaction
    suspend fun replaceGoalsForUser(
        userId: Int,
        goals: List<GoalEntity>
    ) {
        deleteGoalsForUser(userId)
        upsertGoalEntities(goals)
    }
}