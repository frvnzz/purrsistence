package com.example.purrsistence.data.local.repository


import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.domain.time.TimeProvider

interface TrackingRepository {
    suspend fun startTracking(goalId: Int, userId: Int, pauseReminder: Boolean = false): TrackingSession
    suspend fun stopTracking(trackingId: Int)
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSession?
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSession?
}
class TrackingRepositoryImpl (
    private val dao: Dao,
    private val timeProvider: TimeProvider
) : TrackingRepository{

    override suspend fun startTracking(
        goalId: Int,
        userId: Int,
        pauseReminder: Boolean
    ): TrackingSession {
        val session = TrackingSession(
            goalId = goalId,
            userId = userId,
            pauseReminder = pauseReminder,
            startTime = timeProvider.now(),
            endTime = null
        )

        val id = dao.insertTrackingSession(session).toInt()
        return session.copy(trackingId = id)
    }

    override suspend fun stopTracking(trackingId: Int) {
        dao.stopTrackingSession(trackingId, timeProvider.now())
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return dao.getTrackingSessionById(trackingId)
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return dao.getActiveTrackingSession(goalId)
    }
}