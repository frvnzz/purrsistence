package com.example.purrsistence.data.local.repository

import com.example.purrsistence.domain.model.TrackingSession

class FakeTrackingRepository : TrackingRepository {

    private val sessions = mutableListOf<TrackingSession>()
    private var nextId = 1

    override suspend fun insertTrackingSession(session: TrackingSession): TrackingSession {
        val stored = session.copy(id = nextId++)
        sessions.add(stored)
        return stored
    }

    override suspend fun finishTrackingSession(
        trackingId: Int,
        endTimeMillis: Long
    ): TrackingSession? {
        val index = sessions.indexOfFirst { it.id == trackingId }
        if (index == -1) return null

        val old = sessions[index]
        val updated = old.copy(
            endTime = java.time.Instant.ofEpochMilli(endTimeMillis)
        )
        sessions[index] = updated
        return updated
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return sessions.find { it.id == trackingId }
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return sessions.lastOrNull { it.goalId == goalId && it.endTime == null }
    }
}