package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.GoalsDao
import com.example.purrsistence.data.local.dao.TrackingDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface StatisticsRepository {
    fun getGoalsForUser(): Flow<List<Goal>>
    fun getCompletedSessionsForUser(): Flow<List<TrackingSession>>
}

class StatisticsRepositoryImpl(
    private val goalDao: GoalsDao,
    private val trackingDao: TrackingDao
) : StatisticsRepository {
    private val userId = 1

    override fun getGoalsForUser(): Flow<List<Goal>> {
        return goalDao.getGoalsRaw(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedSessionsForUser(): Flow<List<TrackingSession>> {
        return trackingDao.getCompletedSessionsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}