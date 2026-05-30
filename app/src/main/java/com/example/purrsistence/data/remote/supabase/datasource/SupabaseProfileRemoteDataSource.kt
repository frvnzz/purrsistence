package com.example.purrsistence.data.remote.supabase.datasource

import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileSearchParamsDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileSearchResultDto
import com.example.purrsistence.data.remote.supabase.dto.UserSyncStateDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.put

interface ProfileRemoteDataSource {
    suspend fun fetchProfile(userId: String): ProfileDto
    suspend fun updateUsername(userId: String, username: String)
    suspend fun updateAvatarPath(userId: String, avatarPath: String?)
    suspend fun fetchUserSyncState(userId: String): UserSyncStateDto
    suspend fun searchProfiles(query: String): List<ProfileDto>
    suspend fun searchProfilesByUsername(query: String, limit: Int = 10): List<ProfileSearchResultDto>
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

    override suspend fun searchProfiles(query: String): List<ProfileDto> {
        return supabase
            .from("profiles")
            .select(columns = Columns.type<ProfileDto>()) {
                filter {
                    ProfileDto::username ilike "%$query%"
                }
            }
            .decodeList<ProfileDto>()
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

    override suspend fun searchProfilesByUsername(
        query: String,
        limit: Int
    ): List<ProfileSearchResultDto> {
        val trimmedQuery = query.trim()

        require(trimmedQuery.length >= 2) {
            "Search query must contain at least 2 characters."
        }

        return supabase.postgrest
            .rpc(
                function = "search_profiles_by_username",
                parameters = ProfileSearchParamsDto(
                    searchQuery = trimmedQuery,
                    maxResults = limit.coerceIn(1, 20)
                )
            )
            .decodeList<ProfileSearchResultDto>()
    }
}