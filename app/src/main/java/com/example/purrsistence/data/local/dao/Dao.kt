package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.relation.GoalWithSessions
import kotlinx.coroutines.flow.Flow

@Dao

interface Dao {

    // User
    // TODO: handle creation of user
    @Insert
    suspend fun insertUser(user: User)

    // Goal
    @Insert
    suspend fun insertGoal(goal: Goal)

    @androidx.room.Transaction
    @Query("SELECT * FROM Goal WHERE userId = :userId")
    fun getGoals(userId: Int): Flow<List<GoalWithSessions>>

    // Observe total time spent on a goal
    @Query("""
    SELECT SUM(endTime - startTime) 
    FROM TrackingSession 
    WHERE goalId = :goalId
    """)
    fun observeTotalTime(goalId: Int): Flow<Long?>

    @Query("DELETE FROM Goal WHERE goalId = :goalId")
    suspend fun deleteGoal(goalId: Int)

    @Query("SELECT * FROM Goal WHERE goalId = :goalId")
    fun getGoal(goalId: Int): Flow<Goal?>

    @Query("""
    UPDATE Goal 
    SET title = :title, targetDuration = :hours 
    WHERE goalId = :goalId
    """)
    suspend fun updateGoal(goalId: Int, title: String, hours: Int)

    // Tracking Sessions DAO part

    @Insert
    suspend fun insertTrackingSession(session: TrackingSession): Long

    @Query("""
        SELECT * FROM TrackingSession
        WHERE goalId = :goalId and endTime IS NULL
        ORDER BY startTime DESC
        LIMIT 1
    """)
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSession?

    @Query("""
        UPDATE TrackingSession
        SET endTime = :endTime
        WHERE trackingId = :trackingId
    """)
    suspend fun stopTrackingSession(trackingId: Int, endTime: Long)

    @Query("SELECT * FROM TrackingSession WHERE trackingId = :trackingId LIMIT 1")
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSession?

}