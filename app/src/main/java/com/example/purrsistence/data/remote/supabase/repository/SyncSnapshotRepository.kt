package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.remote.supabase.model.SupabaseRemoteUserData
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.User

interface SyncSnapshotRepository {
    suspend fun fetchUserData(
        supabaseUserId: String,
        localUserId: Int
    ): SupabaseRemoteUserData

    suspend fun uploadUserData(
        supabaseUserId: String,
        localUser: User,
        goals: List<Goal>,
        trackingSessions: List<TrackingSession>
    )

    suspend fun uploadGoalsAndTracking(
        supabaseUserId: String,
        goals: List<Goal>,
        trackingSessions: List<TrackingSession>
    )
}


class SyncSnapshotRepositoryImpl(
    private val profileRepository: ProfileRepository,
    private val catRepository: CatCollectionRepository,
    private val goalTrackingRepository: GoalTrackingRepository
) : SyncSnapshotRepository {

    override suspend fun fetchUserData(
        supabaseUserId: String,
        localUserId: Int
    ): SupabaseRemoteUserData {
        val profile =
            profileRepository.getProfile(supabaseUserId)

        val collectedCatIds =
            catRepository
                .getCollectedCatIds(supabaseUserId)
                .distinct()

        val selectedCatIds =
            catRepository
                .getSelectedCatIds(supabaseUserId)
                .distinct()
                .take(5)

        val goals =
            goalTrackingRepository.getGoals(
                supabaseUserId = supabaseUserId,
                localUserId = localUserId
            )

        val trackingSessions =
            goalTrackingRepository.getTrackingSessions(
                supabaseUserId = supabaseUserId,
                localUserId = localUserId
            )

        val remoteUpdatedAt =
            profileRepository.getRemoteUpdatedAt(supabaseUserId)

        return SupabaseRemoteUserData(
            profile = profile,
            collectedCatIds = collectedCatIds,
            selectedCatIds = selectedCatIds,
            goals = goals,
            trackingSessions = trackingSessions,
            remoteUpdatedAt = remoteUpdatedAt
        )
    }

    override suspend fun uploadUserData(
        supabaseUserId: String,
        localUser: User,
        goals: List<Goal>,
        trackingSessions: List<TrackingSession>
    ) {
        profileRepository.updateUsername(
            userId = supabaseUserId,
            username = localUser.username
        )

        profileRepository.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = localUser.profileImageUrl?.toString()
        )

        catRepository.uploadCollectedCats(
            userId = supabaseUserId,
            catIds = localUser.collectedCatsIds
        )

        catRepository.replaceSelectedCats(
            userId = supabaseUserId,
            selectedCatIds = localUser.selectedCatIds
        )

        uploadGoalsAndTracking(
            supabaseUserId = supabaseUserId,
            goals = goals,
            trackingSessions = trackingSessions
        )
    }

    override suspend fun uploadGoalsAndTracking(
        supabaseUserId: String,
        goals: List<Goal>,
        trackingSessions: List<TrackingSession>
    ) {
        goalTrackingRepository.upsertGoals(
            supabaseUserId = supabaseUserId,
            goals = goals
        )

        goalTrackingRepository.upsertTrackingSessions(
            supabaseUserId = supabaseUserId,
            sessions = trackingSessions
        )
    }
}

