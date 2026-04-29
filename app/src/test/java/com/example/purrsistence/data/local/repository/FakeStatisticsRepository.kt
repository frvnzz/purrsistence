package com.example.purrsistence.data.local.repository

import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


class FakeStatisticsRepository(
    private val goals: List<Goal>,
    private val sessions: List<TrackingSession>
) : StatisticsRepository {

    override fun getGoalsForUser(): Flow<List<Goal>> = flowOf(goals)

    override fun getCompletedSessionsForUser(): Flow<List<TrackingSession>> = flowOf(sessions)
}