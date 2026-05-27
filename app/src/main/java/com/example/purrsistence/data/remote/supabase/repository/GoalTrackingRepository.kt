package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toSupabaseDto
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseGoalTrackingRemoteDataSource
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession

interface GoalTrackingRepository {
    suspend fun getGoals(supabaseUserId: String, localUserId: Int): List<Goal>
    suspend fun getTrackingSessions(supabaseUserId: String, localUserId: Int): List<TrackingSession>
    suspend fun deleteTrackingSessions(supabaseUserId: String)
    suspend fun upsertGoals(supabaseUserId: String, goals: List<Goal>)
    suspend fun upsertTrackingSessions(supabaseUserId: String, sessions: List<TrackingSession>)
}

class GoalTrackingRepositoryImpl(
    private val remoteDataSource: SupabaseGoalTrackingRemoteDataSource
) : GoalTrackingRepository {

    override suspend fun getGoals(
        supabaseUserId: String,
        localUserId: Int
    ): List<Goal> {
        return remoteDataSource
            .fetchGoals(supabaseUserId)
            .map { goalDto ->
                goalDto.toDomain(localUserId)
            }
    }

    override suspend fun getTrackingSessions(
        supabaseUserId: String,
        localUserId: Int
    ): List<TrackingSession> {
        return remoteDataSource
            .fetchTrackingSessions(supabaseUserId)
            .map { trackingSessionDto ->
                trackingSessionDto.toDomain(localUserId)
            }
    }

    override suspend fun deleteTrackingSessions(supabaseUserId: String) {
        remoteDataSource.deleteTrackingSessions(supabaseUserId)
    }

    override suspend fun upsertGoals(
        supabaseUserId: String,
        goals: List<Goal>
    ) {
        if (goals.isEmpty()) {
            return
        }

        remoteDataSource.upsertGoals(
            goals.map { goal ->
                goal.toSupabaseDto(supabaseUserId)
            }
        )
    }

    override suspend fun upsertTrackingSessions(
        supabaseUserId: String,
        sessions: List<TrackingSession>
    ) {
        if (sessions.isEmpty()) {
            return
        }

        remoteDataSource.upsertTrackingSessions(
            sessions.map { session ->
                session.toSupabaseDto(supabaseUserId)
            }
        )
    }
}