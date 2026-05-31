package com.example.purrsistence.data.remote.supabase.datasource

import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

interface FriendshipRemoteDataSource {
    suspend fun fetchAcceptedFriendProfiles(userId: String): List<ProfileDto>
    suspend fun fetchIncomingRequests(userId: String): List<FriendshipDto>
    suspend fun fetchOutgoingRequests(userId: String): List<FriendshipDto>
    suspend fun sendFriendRequest(currentUserId: String, friendUserId: String)
    suspend fun acceptFriendRequest(friendshipId: Long)
    suspend fun declineFriendRequest(friendshipId: Long)
    suspend fun deleteFriendship(friendshipId: Long)
}

class SupabaseFriendshipRemoteDataSource(
    private val supabase: SupabaseClient
) : FriendshipRemoteDataSource {

    override suspend fun fetchAcceptedFriendProfiles(
        userId: String
    ): List<ProfileDto> {
        val asRequester = supabase
            .from("friendships")
            .select {
                filter {
                    eq("requester_id", userId)
                    eq("status", "accepted")
                }
            }
            .decodeList<FriendshipDto>()

        val asAddressee = supabase
            .from("friendships")
            .select {
                filter {
                    eq("addressee_id", userId)
                    eq("status", "accepted")
                }
            }
            .decodeList<FriendshipDto>()

        val friendIds = buildSet {
            asRequester.forEach { add(it.addresseeId) }
            asAddressee.forEach { add(it.requesterId) }
        }

        return friendIds.map { friendId ->
            supabase
                .from("profiles")
                .select {
                    filter {
                        eq("id", friendId)
                    }
                }
                .decodeSingle<ProfileDto>()
        }
    }

    override suspend fun fetchIncomingRequests(
        userId: String
    ): List<FriendshipDto> {
        return supabase
            .from("friendships")
            .select(Columns.raw("*, requester:profiles!requester_id(*)")) {
                filter {
                    eq("addressee_id", userId)
                    eq("status", "pending")
                }
            }
            .decodeList<FriendshipDto>()
    }

    override suspend fun fetchOutgoingRequests(
        userId: String
    ): List<FriendshipDto> {
        return supabase
            .from("friendships")
            .select(Columns.raw("*, addressee:profiles!addressee_id(*)")) {
                filter {
                    eq("requester_id", userId)
                    eq("status", "pending")
                }
            }
            .decodeList<FriendshipDto>()
    }

    override suspend fun sendFriendRequest(
        currentUserId: String,
        friendUserId: String
    ) {
        val row = FriendshipDto(
            requesterId = currentUserId,
            addresseeId = friendUserId,
            status = "pending"
        )

        supabase
            .from("friendships")
            .insert(row)
    }

    override suspend fun acceptFriendRequest(friendshipId: Long) {
        updateFriendshipStatus(friendshipId, "accepted")
    }

    override suspend fun declineFriendRequest(friendshipId: Long) {
        updateFriendshipStatus(friendshipId, "declined")
    }

    override suspend fun deleteFriendship(friendshipId: Long) {
        supabase
            .from("friendships")
            .delete {
                filter {
                    eq("id", friendshipId)
                }
            }
    }

    private suspend fun updateFriendshipStatus(
        friendshipId: Long,
        status: String
    ) {
        supabase
            .from("friendships")
            .update(
                {
                    set("status", status)
                }
            ) {
                filter {
                    eq("id", friendshipId)
                }
            }
    }
}