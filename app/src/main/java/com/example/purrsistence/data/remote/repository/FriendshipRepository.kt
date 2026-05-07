package com.example.purrsistence.data.remote.repository

import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.FriendshipRemoteDataSource
import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileDto

class FriendshipRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val friendshipRemoteDataSource: FriendshipRemoteDataSource
) {

    suspend fun getFriends(): List<ProfileDto> {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        return friendshipRemoteDataSource.fetchAcceptedFriendProfiles(
            userId = supabaseUserId
        )
    }

    suspend fun getIncomingRequests(): List<FriendshipDto> {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        return friendshipRemoteDataSource.fetchIncomingRequests(
            userId = supabaseUserId
        )
    }

    suspend fun getOutgoingRequests(): List<FriendshipDto> {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        return friendshipRemoteDataSource.fetchOutgoingRequests(
            userId = supabaseUserId
        )
    }

    suspend fun sendFriendRequest(friendUserId: String) {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        friendshipRemoteDataSource.sendFriendRequest(
            currentUserId = supabaseUserId,
            friendUserId = friendUserId
        )
    }

    suspend fun acceptRequest(friendshipId: Long) {
        friendshipRemoteDataSource.acceptFriendRequest(friendshipId)
    }

    suspend fun declineRequest(friendshipId: Long) {
        friendshipRemoteDataSource.declineFriendRequest(friendshipId)
    }

    suspend fun removeFriendship(friendshipId: Long) {
        friendshipRemoteDataSource.deleteFriendship(friendshipId)
    }
}