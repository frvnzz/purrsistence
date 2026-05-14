package com.example.purrsistence.data.local.repository

import com.example.purrsistence.domain.model.TrackingSession
import java.time.Instant

class FakeTrackingRepository : TrackingRepository {

    private val sessions = mutableListOf<TrackingSession>()
    private var nextId = 1

    var cleanupCalls = 0
    var finishCalls = 0
    var insertCalls = 0

    override suspend fun insertTrackingSession(session: TrackingSession): TrackingSession {
        insertCalls++
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

    override suspend fun deleteFinishedSessionsForGoalBefore(
        goalId: Int,
        cutoff: Instant
    ) {
        cleanupCalls++
        sessions.removeAll {
            it.goalId == goalId &&
                    it.endTime != null &&
                    it.endTime.isBefore(cutoff)
        }
    }

    override suspend fun countSessionsForGoal(goalId: Int): Int {
        return sessions.count { it.goalId == goalId }
    }

    override suspend fun getTrackingSessionsForSync(userId: Int): List<TrackingSession> {
        TODO("Not yet implemented")
    }

    override suspend fun replaceTrackingSessionsFromRemoteSync(
        userId: Int,
        sessions: List<TrackingSession>
    ) {
        TODO("Not yet implemented")
    }

    fun seedSession(session: TrackingSession) {
        sessions.add(session)
        nextId = maxOf(nextId, session.id + 1)
    }

    fun getSessionsForGoal(goalId: Int): List<TrackingSession> {
        return sessions.filter { it.goalId == goalId }
    }
}