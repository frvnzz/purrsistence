package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
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

    @Query("SELECT * FROM GoalEntity WHERE userId = :userId")
    fun getGoalsRaw(userId: Int): Flow<List<GoalEntity>> //get only the goals data

    @Query("DELETE FROM GoalEntity WHERE goalId = :goalId")
    suspend fun deleteGoal(goalId: Int)

    @Query("SELECT * FROM GoalEntity WHERE goalId = :goalId")
    fun getGoal(goalId: Int): Flow<GoalEntity?>

    @Query(
        """
    UPDATE GoalEntity 
    SET title = :title,
        type = :type,
        targetDuration = :hours,
        deepFocus = :deepFocus
    WHERE goalId = :goalId
    """
    )
    suspend fun updateGoal(
        goalId: Int,
        title: String,
        type: String,
        hours: Int,
        deepFocus: Boolean
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

}