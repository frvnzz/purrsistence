package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.FriendshipRepository
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.net.URL

class ProfileService(
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository? = null
) {

    fun getProfile(userId: Int): Flow<UserProfile?> {
        val userFlow = userRepository.getUser(userId)
        val friendsFlow = friendshipRepository?.getFriends(userId) ?: flowOf(emptyList())

        return combine(userFlow, friendsFlow) { user, friends ->
            user?.toProfile(friends)
        }
    }

    suspend fun updateProfile(
        userId: Int,
        username: String,
        profileImageUrl: URL?
    ) {
        val currentUser = userRepository.getUser(userId).firstOrNull() ?: return

        val updatedUser = currentUser.copy(
            username = username,
            profileImageUrl = profileImageUrl
        )

        userRepository.updateUser(updatedUser)
    }

    suspend fun updateProfilePicture(
        userId: Int,
        profileImageUrl: URL?
    ) {
        val currentUser = userRepository.getUser(userId).firstOrNull() ?: return

        val updatedUser = currentUser.copy(
            profileImageUrl = profileImageUrl
        )

        userRepository.updateUser(updatedUser)
    }

    private fun User.toProfile(friends: List<User>): UserProfile {
        return UserProfile(
            userId = id,
            supabaseUserId = supabaseUserId,
            username = username,
            profileImageUrl = profileImageUrl as String?,
            balance = balance,
            friends = friends.map {
                FriendProfile(
                    userId = it.id,
                    username = it.username,
                    profileImageUrl = it.profileImageUrl as String?
                )
            },
            collectedCatIds = collectedCatsIds,
            isSupabaseLinked = !supabaseUserId.isNullOrBlank()
        )
    }
}