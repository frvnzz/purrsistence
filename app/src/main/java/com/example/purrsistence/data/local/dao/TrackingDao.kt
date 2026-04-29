package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingDao {

    // Tracking Sessions DAO part

    @Insert
    suspend fun insertTrackingSession(session: TrackingSessionEntity): Long

    @Query(
        """
        SELECT * FROM TrackingSessionEntity
        WHERE goalId = :goalId and endTime IS NULL
        ORDER BY startTime DESC
        LIMIT 1
    """
    )
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSessionEntity?

    @Query(
        """
        UPDATE TrackingSessionEntity
        SET endTime = :endTime
        WHERE trackingId = :trackingId
    """
    )
    suspend fun stopTrackingSession(trackingId: Int, endTime: Long)

    @Query("SELECT * FROM TrackingSessionEntity WHERE trackingId = :trackingId LIMIT 1")
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSessionEntity?

    // -> Observe total time spent on a goal
    @Query(
        """
    SELECT SUM(endTime - startTime) 
    FROM TrackingSessionEntity 
    WHERE goalId = :goalId
    """
    )
    fun observeTotalTime(goalId: Int): Flow<Long?>

    @Query(
        """
        SELECT ts.* FROM TrackingSessionEntity ts
        INNER JOIN GoalEntity g ON ts.goalId = g.goalId
        WHERE g.userId = :userId
        AND ts.endTime IS NOT NULL
    """
    )
    fun getCompletedSessionsForUser(userId: Int): Flow<List<TrackingSessionEntity>> //get the sessions that are completed and not ongoing
}