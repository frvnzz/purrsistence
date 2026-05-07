package com.example.purrsistence.data.remote.repository

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.ProfileRemoteDataSource

class ProfileRepository(
    private val userDao: UserDao,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val profileRemoteDataSource: ProfileRemoteDataSource
) {

    suspend fun syncCurrentProfileIntoLocalUser(
        localUserId: Long
    ) {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        val profile = profileRemoteDataSource.fetchProfile(supabaseUserId)

        userDao.linkSupabaseProfileToLocalUser(
            localUserId = localUserId,
            supabaseUserId = profile.id,
            username = profile.username,
            avatarPath = profile.avatarPath
        )
    }

    suspend fun updateUsername(
        localUserId: Long,
        username: String
    ) {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = username
        )

        userDao.linkSupabaseProfileToLocalUser(
            localUserId = localUserId,
            supabaseUserId = supabaseUserId,
            username = username,
            avatarPath = userDao.getUserBySupabaseId(supabaseUserId)?.profileImageUrl
        )
    }

    suspend fun updateAvatarPath(
        localUserId: Long,
        avatarPath: String
    ) {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = avatarPath
        )

        val existingUser = userDao.getUserBySupabaseId(supabaseUserId)

        userDao.linkSupabaseProfileToLocalUser(
            localUserId = localUserId,
            supabaseUserId = supabaseUserId,
            username = existingUser?.username ?: "User",
            avatarPath = avatarPath
        )
    }
}