package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.local.mapping.remoteUpdatedAtAsInstant
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toFriendProfile
import com.example.purrsistence.data.remote.supabase.datasource.ProfileRemoteDataSource
import com.example.purrsistence.domain.model.FriendProfile
import java.time.Instant

interface ProfileRepository {
    suspend fun getProfile(userId: String): FriendProfile
    suspend fun updateUsername(userId: String, username: String)
    suspend fun updateAvatarPath(userId: String, avatarPath: String?)
    suspend fun getRemoteUpdatedAt(userId: String): Instant
    suspend fun searchProfiles(query: String, limit: Int = 10): List<FriendProfile>
    suspend fun fetchFriendProfile(userId: String): FriendProfile
}

class ProfileRepositoryImpl(
    private val remoteDataSource: ProfileRemoteDataSource
) : ProfileRepository{

    override suspend fun getProfile(
        userId: String
    ): FriendProfile {
        return remoteDataSource
            .fetchProfile(userId)
            .toFriendProfile()
    }

    override suspend fun searchProfiles(
        query: String,
        limit: Int
    ): List<FriendProfile> {
        val trimmedQuery = query.trim()

        if (trimmedQuery.length < 2) {
            return emptyList()
        }

        return remoteDataSource
            .searchProfilesByUsername(
                query = trimmedQuery,
                limit = limit
            )
            .map { result ->
                FriendProfile(
                    id = result.id,
                    username = result.username,
                    avatarPath = result.avatarPath
                )
            }
    }

    override suspend fun fetchFriendProfile(
        userId: String
    ): FriendProfile {
        val profile = remoteDataSource.fetchProfile(userId)

        return FriendProfile(
            id = profile.id,
            username = profile.username,
            avatarPath = profile.avatarPath
        )
    }

    override suspend fun updateUsername(
        userId: String,
        username: String
    ) {
        remoteDataSource.updateUsername(
            userId = userId,
            username = username
        )
    }

    override suspend fun updateAvatarPath(
        userId: String,
        avatarPath: String?
    ) {
        remoteDataSource.updateAvatarPath(
            userId = userId,
            avatarPath = avatarPath
        )
    }

    override suspend fun getRemoteUpdatedAt(
        userId: String
    ): Instant {
        return remoteDataSource
            .fetchUserSyncState(userId)
            .remoteUpdatedAtAsInstant()
    }
}