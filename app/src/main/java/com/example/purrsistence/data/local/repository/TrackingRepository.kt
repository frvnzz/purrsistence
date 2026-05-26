package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.TrackingDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.TrackingSession
import java.time.Duration
import java.time.Instant

// TODO: refactor logic to service layer (Ramon) :)

interface TrackingRepository {
    suspend fun insertTrackingSession(session: TrackingSession): TrackingSession
    suspend fun finishTrackingSession(trackingId: Int, endTimeMillis: Long): TrackingSession?
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSession?
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSession?
    suspend fun getAnyActiveTrackingSession(): TrackingSession?
    suspend fun deleteFinishedSessionsForGoalBefore(goalId: Int, cutoff: Instant)
    suspend fun countSessionsForGoal(goalId: Int): Int
    suspend fun deleteAllTrackingSessions(userId: Int)
    suspend fun getTrackingSessionsForSync(userId: Int): List<TrackingSession>
    suspend fun replaceTrackingSessionsFromRemoteSync(
        userId: Int,
        sessions: List<TrackingSession>
    )
    suspend fun updateTrackingSession(session: TrackingSession)
}

class TrackingRepositoryImpl(
    private val trackingDao: TrackingDao
) : TrackingRepository {

    override suspend fun insertTrackingSession(session: TrackingSession): TrackingSession {
        val entity = session.toEntity()
        val id = trackingDao.insertTrackingSession(entity).toInt()
        return session.copy(id = id)
    }

    override suspend fun finishTrackingSession(
        trackingId: Int,
        endTimeMillis: Long
    ): TrackingSession? {
        val sessionEntity = trackingDao.getTrackingSessionById(trackingId) ?: return null
        val endTime = Instant.ofEpochMilli(endTimeMillis)

        val updatedEntity = if (sessionEntity.currentPauseStart != null) {
            val pauseStart = Instant.ofEpochMilli(sessionEntity.currentPauseStart)
            val pauseDuration = Duration.between(pauseStart, endTime).toMillis()

            //write pause history string
            val parts = sessionEntity.pauseHistory.split(";")
            val baseTotal = parts.getOrNull(0)?.toLongOrNull() ?: 0L
            val intervals = parts.getOrNull(1) ?: ""
            val newIntervals = if (intervals.isEmpty()) "${sessionEntity.currentPauseStart}-$endTimeMillis" else "$intervals,${sessionEntity.currentPauseStart}-$endTimeMillis"
            val newTotal = baseTotal + pauseDuration

            sessionEntity.copy(
                endTime = endTimeMillis,
                pauseHistory = "$newTotal;$newIntervals",
                currentPauseStart = null
            )
        } else {
            sessionEntity.copy(endTime = endTimeMillis)
        }

        trackingDao.updateTrackingSession(updatedEntity)
        return updatedEntity.toDomain()
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return trackingDao.getTrackingSessionById(trackingId)?.toDomain()
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return trackingDao.getActiveTrackingSession(goalId)?.toDomain()
    }

    // check if there is ANY currently active TrackingSession (for restoring purposes)
    override suspend fun getAnyActiveTrackingSession(): TrackingSession? {
        return trackingDao.getAnyActiveTrackingSession()?.toDomain()
    }

    override suspend fun deleteFinishedSessionsForGoalBefore(
        goalId: Int,
        cutoff: Instant
    ) {
        trackingDao.deleteFinishedSessionsForGoalBefore(
            goalId = goalId,
            cutoffMillis = cutoff.toEpochMilli()
        )
    }

    override suspend fun countSessionsForGoal(goalId: Int): Int {
        return trackingDao.countSessionsForGoal(goalId)
    }

    override suspend fun deleteAllTrackingSessions(userId: Int) {
        trackingDao.deleteTrackingSessionsForUser(userId)
    }

    override suspend fun updateTrackingSession(session: TrackingSession) {
        val entity = session.toEntity()
        trackingDao.updatePauseData(
            entity.trackingId,
            entity.pauseHistory,
            entity.currentPauseStart,
            entity.lastResetTime
        )
    }

    override suspend fun getTrackingSessionsForSync(
        userId: Int
    ): List<TrackingSession> {
        return trackingDao
            .getTrackingSessionEntitiesForUser(userId)
            .map { it.toDomain() }
    }

    override suspend fun replaceTrackingSessionsFromRemoteSync(
        userId: Int,
        sessions: List<TrackingSession>
    ) {
        trackingDao.replaceTrackingSessionsForUser(
            userId = userId,
            sessions = sessions.map { session ->
                session.copy(userId = userId).toEntity()
            }
        )
    }
}