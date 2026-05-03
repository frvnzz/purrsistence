package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.TrackingDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.TrackingSession
import java.time.Instant

// TODO: refactor logic to service layer (Ramon) :)

interface TrackingRepository {
    suspend fun insertTrackingSession(session: TrackingSession): TrackingSession
    suspend fun finishTrackingSession(trackingId: Int, endTimeMillis: Long): TrackingSession?
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSession?
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSession?
    suspend fun deleteFinishedSessionsForGoalBefore(goalId: Int, cutoff: java.time.Instant)
    suspend fun countSessionsForGoal(goalId: Int): Int
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
        trackingDao.stopTrackingSession(trackingId, endTimeMillis)
        return trackingDao.getTrackingSessionById(trackingId)?.toDomain()
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return trackingDao.getTrackingSessionById(trackingId)?.toDomain()
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return trackingDao.getActiveTrackingSession(goalId)?.toDomain()
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
}