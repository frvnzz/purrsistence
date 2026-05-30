package com.example.purrsistence.data.remote.supabase.datasource

import com.example.purrsistence.data.remote.supabase.dto.GoalsDto
import com.example.purrsistence.data.remote.supabase.dto.TrackingSessionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseGoalTrackingRemoteDataSource(
    private val supabase: SupabaseClient
) {

    suspend fun fetchGoals(
        supabaseUserId: String
    ): List<GoalsDto> {
        return supabase
            .from("goals")
            .select {
                filter {
                    eq("user_id", supabaseUserId)
                }
            }
            .decodeList<GoalsDto>()
    }

    suspend fun upsertGoals(
        goals: List<GoalsDto>
    ) {
        if (goals.isEmpty()) return

        supabase
            .from("goals")
            .upsert(goals) {
                onConflict = "user_id,goal_id"
            }
    }

    suspend fun fetchTrackingSessions(
        supabaseUserId: String
    ): List<TrackingSessionDto> {
        return supabase
            .from("tracking_sessions")
            .select {
                filter {
                    eq("user_id", supabaseUserId)
                }
            }
            .decodeList<TrackingSessionDto>()
    }

    suspend fun upsertTrackingSessions(
        sessions: List<TrackingSessionDto>
    ) {
        if (sessions.isEmpty()) return

        supabase
            .from("tracking_sessions")
            .upsert(sessions) {
                onConflict = "user_id,tracking_id"
            }
    }

    suspend fun deleteGoal(
        supabaseUserId: String,
        goalId: Int
    ) {
        supabase
            .from("goals")
            .delete {
                filter {
                    eq("user_id", supabaseUserId)
                    eq("goal_id", goalId)
                }
            }
    }

    suspend fun deleteTrackingSession(
        supabaseUserId: String,
        trackingId: Int
    ) {
        supabase
            .from("tracking_sessions")
            .delete {
                filter {
                    eq("user_id", supabaseUserId)
                    eq("tracking_id", trackingId)
                }
            }
    }

    suspend fun deleteTrackingSessions(
        supabaseUserId: String
    ) {
        supabase
            .from("tracking_sessions")
            .delete {
                filter {
                    eq("user_id", supabaseUserId)
                }
            }
    }
}