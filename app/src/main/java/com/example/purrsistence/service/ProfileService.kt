package com.example.purrsistence.service

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.purrsistence.data.local.repository.FriendshipRepository
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.model.UserProfile
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.net.URL

class ProfileService(
    private val context: Context,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider,
    private val friendshipRepository: FriendshipRepository? = null
) {

    fun getProfile(userId: Int): Flow<UserProfile?> {
        val userFlow = userRepository.getUser(userId)

        val friendsFlow: Flow<List<User>> =
            friendshipRepository?.getFriends(userId) ?: flowOf(emptyList())

        return combine(userFlow, friendsFlow) { user, friends ->
            user?.toProfile(friends)
        }
    }

    suspend fun updateProfile(
        userId: Int,
        username: String,
        profileImageUrl: URL?
    ) {
        val currentUser =
            userRepository.getUser(userId).firstOrNull() ?: return

        val updatedUser = currentUser.copy(
            username = username,
            profileImageUrl = profileImageUrl
        )

        userRepository.updateUserFromLocalAction(updatedUser)
    }

    suspend fun updateProfilePicture(
        userId: Int,
        profileImageUrl: String?
    ) {
        val currentUser =
            userRepository.getUser(userId).firstOrNull() ?: return

        val urlObject = profileImageUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { persistProfileImageReference(it) }

        val updatedUser = currentUser.copy(
            profileImageUrl = urlObject
        )

        userRepository.updateUserFromLocalAction(updatedUser)
    }

    private fun User.toProfile(friends: List<User>): UserProfile {
        return UserProfile(
            userId = id,
            supabaseUserId = supabaseUserId,
            username = username,
            profileImageUrl = profileImageUrl?.toString(),
            balance = balance,
            friends = friends.map { friend ->
                FriendProfile(
                    id = friend.id.toString(),
                    username = friend.username,
                    avatarPath = friend.profileImageUrl?.toString()
                )
            },
            collectedCatIds = collectedCatsIds,
            isSupabaseLinked = !supabaseUserId.isNullOrBlank()
        )
    }

    private fun persistProfileImageReference(
        profileImageReference: String
    ): URL? {
        val uri = profileImageReference.toUri()

        return when (uri.scheme?.lowercase()) {
            "content" -> copyContentUriToCache(uri)
                ?.toURI()
                ?.toURL()

            "file" -> runCatching {
                File(requireNotNull(uri.path)).toURI().toURL()
            }.getOrNull()

            "http", "https" -> runCatching {
                URL(profileImageReference)
            }.getOrNull()

            else -> runCatching {
                URL(profileImageReference)
            }.getOrNull()
        }
    }

    private fun copyContentUriToCache(uri: Uri): File? {
        val imageDir = File(
            context.cacheDir,
            "profile-images"
        ).apply {
            mkdirs()
        }

        val targetFile = File(
            imageDir,
            "profile-${timeProvider.now().toEpochMilli()}.jpg"
        )

        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            targetFile
        }.getOrNull()
    }
}