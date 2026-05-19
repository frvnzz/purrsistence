package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingDao {

    // Tracking Sessions DAO part

    @Insert
    suspend fun insertTrackingSession(session: TrackingSessionEntity): Long

    @Update
    suspend fun updateTrackingSession(session: TrackingSessionEntity)

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
    SELECT * FROM TrackingSessionEntity
    WHERE endTime IS NULL
    ORDER BY startTime DESC
    LIMIT 1
    """
    )
    suspend fun getAnyActiveTrackingSession(): TrackingSessionEntity?

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

    //Observe total time spent on a goal - the pause time which is the first number in the pauseHistory before ;
    @Query(
        """
    SELECT SUM(endTime - startTime - CAST(SUBSTR(pauseHistory, 1, INSTR(pauseHistory, ';') - 1) AS INTEGER))
    FROM TrackingSessionEntity 
    WHERE goalId = :goalId AND endTime IS NOT NULL
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

    @Query("""
    DELETE FROM TrackingSessionEntity
    WHERE goalId = :goalId
      AND endTime IS NOT NULL
      AND endTime < :cutoffMillis
""")
    suspend fun deleteFinishedSessionsForGoalBefore(goalId: Int, cutoffMillis: Long)

    @Query("SELECT COUNT(*) FROM TrackingSessionEntity WHERE goalId = :goalId")
    suspend fun countSessionsForGoal(goalId: Int): Int

    @Query("UPDATE TrackingSessionEntity SET pauseHistory = :history, currentPauseStart = :pauseStart, lastResetTime = :lastReset WHERE trackingId = :id")
    suspend fun updatePauseData(id: Int, history: String, pauseStart: Long?, lastReset: Long?)

    @Query(
        """
    SELECT *
    FROM TrackingSessionEntity
    WHERE userId = :userId
    """
    )
    suspend fun getTrackingSessionEntitiesForUser(
        userId: Int
    ): List<TrackingSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrackingSessionEntities(
        sessions: List<TrackingSessionEntity>
    )

    @Query(
        """
        DELETE FROM TrackingSessionEntity
        WHERE userId = :userId
        """
    )
    suspend fun deleteTrackingSessionsForUser(
        userId: Int
    )

    @Transaction
    suspend fun replaceTrackingSessionsForUser(
        userId: Int,
        sessions: List<TrackingSessionEntity>
    ) {
        deleteTrackingSessionsForUser(userId)
        upsertTrackingSessionEntities(sessions)
    }
}