package com.example.purrsistence.data.remote.supabase.model

import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import java.time.Instant

data class SupabaseRemoteUserData(
    val profile: FriendProfile,
    val collectedCatIds: List<String>,
    val selectedCatIds: List<String>,
    val goals: List<Goal>,
    val trackingSessions: List<TrackingSession>,
    val remoteUpdatedAt: Instant
)