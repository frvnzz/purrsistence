package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.FriendshipRemoteDataSource
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Friendship

interface FriendshipRepository {
    suspend fun getFriends(userId: String): List<FriendProfile>
    suspend fun getIncomingRequests(userId: String): List<Friendship>
    suspend fun getOutgoingRequests(userId: String): List<Friendship>

    suspend fun sendFriendRequest(
        currentUserId: String,
        friendUserId: String
    )

    suspend fun acceptFriendRequest(friendshipId: Long)
    suspend fun declineFriendRequest(friendshipId: Long)
    suspend fun deleteFriendship(friendshipId: Long)
}

class FriendshipRepositoryImpl(
    private val friendshipRemoteDataSource: FriendshipRemoteDataSource
) : FriendshipRepository{

    override suspend fun getFriends(
        userId: String
    ): List<FriendProfile> {
        return friendshipRemoteDataSource
            .fetchAcceptedFriendProfiles(userId)
            .map { profileDto -> profileDto.toDomain() }
    }

    override suspend fun getIncomingRequests(
        userId: String
    ): List<Friendship> {
        return friendshipRemoteDataSource
            .fetchIncomingRequests(userId)
            .map { friendshipDto -> friendshipDto.toDomain() }
    }

    override suspend fun getOutgoingRequests(
        userId: String
    ): List<Friendship> {
        return friendshipRemoteDataSource
            .fetchOutgoingRequests(userId)
            .map { friendshipDto -> friendshipDto.toDomain() }
    }

    override suspend fun sendFriendRequest(
        currentUserId: String,
        friendUserId: String
    ) {
        friendshipRemoteDataSource.sendFriendRequest(
            currentUserId = currentUserId,
            friendUserId = friendUserId
        )
    }

    override suspend fun acceptFriendRequest(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.acceptFriendRequest(friendshipId)
    }

    override suspend fun declineFriendRequest(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.declineFriendRequest(friendshipId)
    }

    override suspend fun deleteFriendship(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.deleteFriendship(friendshipId)
    }
}