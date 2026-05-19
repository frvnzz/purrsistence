package com.example.purrsistence.data.remote.supabase.datasource

import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import com.example.purrsistence.data.remote.supabase.dto.UserSyncStateDto

interface ProfileRemoteDataSource {
    suspend fun fetchProfile(userId: String): ProfileDto
    suspend fun updateUsername(userId: String, username: String)
    suspend fun updateAvatarPath(userId: String, avatarPath: String?)
    suspend fun fetchUserSyncState(userId: String): UserSyncStateDto
}

class SupabaseProfileRemoteDataSource(
    private val supabase: SupabaseClient
) : ProfileRemoteDataSource {

    override suspend fun fetchProfile(userId: String): ProfileDto {
        return supabase
            .from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<ProfileDto>()
    }

    override suspend fun updateUsername(
        userId: String,
        username: String
    ) {
        supabase
            .from("profiles")
            .update(
                {
                    set("username", username)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
    }

    override suspend fun updateAvatarPath(
        userId: String,
        avatarPath: String?
    ) {
        supabase
            .from("profiles")
            .update(
                {
                    set("avatar_path", avatarPath)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
    }

    override suspend fun fetchUserSyncState(userId: String): UserSyncStateDto {
        return supabase
            .from("user_sync_state")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeSingle<UserSyncStateDto>()
    }
}