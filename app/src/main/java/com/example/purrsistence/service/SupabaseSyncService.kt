package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseAuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseFriendshipRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseProfileRemoteDataSource
import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.model.types.SyncStatus
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.OffsetDateTime

class SupabaseSyncService(
    private val userRepository: UserRepository,
    private val authRemoteDataSource: SupabaseAuthRemoteDataSource,
    private val profileRemoteDataSource: SupabaseProfileRemoteDataSource,
    private val catRemoteDataSource: SupabaseCatRemoteDataSource,
    private val friendshipRemoteDataSource: SupabaseFriendshipRemoteDataSource,
    private val localUserId: Int = 1
) {
    fun isSignedIn(): Boolean {
        return authRemoteDataSource.currentUserId() != null
    }

    suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        authRemoteDataSource.signUp(
            email = email,
            password = password,
            username = username
        )
    }

    suspend fun signIn(
        email: String,
        password: String
    ) {
        authRemoteDataSource.signIn(
            email = email,
            password = password
        )

        syncUserAfterSignIn()
    }

    suspend fun signOut() {
        authRemoteDataSource.signOut()
    }

    fun currentSupabaseUserId(): String? {
        return authRemoteDataSource.currentUserId()
    }

    private suspend fun syncUserAfterSignIn(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()
        val remoteData = fetchRemoteUserData(supabaseUserId)

        val mergedCollectedCatIds =
            (localUser.collectedCatsIds + remoteData.collectedCatIds)
                .distinct()

        val mergedSelectedCatIds =
            (localUser.selectedCatIds + remoteData.selectedCatIds)
                .distinct()
                .filter { it in mergedCollectedCatIds }

        val useLocalProfileData =
            localUser.hasPendingLocalChanges &&
                    localUser.localUpdatedAt?.isAfter(
                        parseSupabaseTimestamp(remoteData.remoteUpdatedAt)
                    ) == true

        val mergedUsername =
            if (useLocalProfileData) {
                localUser.username
            } else {
                remoteData.profile.username
            }

        val mergedAvatarPath =
            if (useLocalProfileData) {
                localUser.profileImageUrl?.toString()
            } else {
                remoteData.profile.avatarPath
            }

        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = mergedUsername
        )

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = mergedAvatarPath
        )

        catRemoteDataSource.uploadLocalCollectedCats(
            userId = supabaseUserId,
            catIds = mergedCollectedCatIds
        )

        catRemoteDataSource.replaceSelectedCats(
            userId = supabaseUserId,
            selectedCatIds = mergedSelectedCatIds
        )

        val mergedUser = localUser.copy(
            username = mergedUsername,
            profileImageUrl = mergedAvatarPath?.let { java.net.URL(it) },
            collectedCatsIds = mergedCollectedCatIds,
            selectedCatIds = mergedSelectedCatIds,
            isSupabaseLinked = true,
            supabaseUserId = supabaseUserId
        )

        userRepository.updateUserFromRemoteSync(mergedUser)
        userRepository.markUserSynced(localUserId)

        return SyncStatus.IN_SYNC
    }

    suspend fun checkAndSyncIfNeeded(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()

        val remoteData = fetchRemoteUserData(supabaseUserId)

        val localComparable = SyncComparableUserData.fromLocal(localUser)
        val remoteComparable = SyncComparableUserData.fromRemote(remoteData)

        if (localComparable == remoteComparable) {
            userRepository.markUserSynced(localUserId)
            return SyncStatus.IN_SYNC
        }

        val localUpdatedAt = localUser.localUpdatedAt

        val remoteUpdatedAt =
            parseSupabaseTimestamp(remoteData.remoteUpdatedAt)

        return when {
            localUser.hasPendingLocalChanges &&
                    localUpdatedAt?.isAfter(remoteUpdatedAt) == true -> {
                uploadLocalUserToSupabase(
                    localUser = localUser,
                    supabaseUserId = supabaseUserId
                )

                userRepository.markUserSynced(localUserId)

                SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
            }

            else -> {
                applyRemoteUserToLocal(
                    localUser = localUser,
                    supabaseUserId = supabaseUserId,
                    remoteData = remoteData
                )

                SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE
            }
        }
    }

    suspend fun forceUploadLocalToSupabase(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()

        uploadLocalUserToSupabase(
            localUser = localUser,
            supabaseUserId = supabaseUserId
        )

        userRepository.markUserSynced(localUserId)

        return SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
    }

    suspend fun forceDownloadFromSupabase(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()
        val remoteData = fetchRemoteUserData(supabaseUserId)

        applyRemoteUserToLocal(
            localUser = localUser,
            supabaseUserId = supabaseUserId,
            remoteData = remoteData
        )

        return SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE
    }

    suspend fun addCollectedCatToSupabaseAndLocal(
        catId: String
    ) {
        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        catRemoteDataSource.addCollectedCat(
            userId = supabaseUserId,
            catId = catId
        )

        val updatedUser = localUser.copy(
            collectedCatsIds = (localUser.collectedCatsIds + catId).distinct(),
            isSupabaseLinked = true,
            supabaseUserId = supabaseUserId
        )

        userRepository.updateUserFromRemoteSync(updatedUser)
    }

    suspend fun updateUsername(
        username: String
    ) {
        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = username
        )

        userRepository.updateUserFromRemoteSync(
            localUser.copy(
                username = username,
                isSupabaseLinked = true,
                supabaseUserId = supabaseUserId
            )
        )
    }

    suspend fun updateAvatarPath(
        avatarPath: String?
    ) {
        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = avatarPath
        )

        userRepository.updateUserFromRemoteSync(
            localUser.copy(
                profileImageUrl = avatarPath?.let { java.net.URL(it) },
                isSupabaseLinked = true,
                supabaseUserId = supabaseUserId
            )
        )
    }

    suspend fun getFriends(): List<ProfileDto> {
        return friendshipRemoteDataSource.fetchAcceptedFriendProfiles(
            userId = requireSupabaseUserId()
        )
    }

    suspend fun getIncomingFriendRequests(): List<FriendshipDto> {
        return friendshipRemoteDataSource.fetchIncomingRequests(
            userId = requireSupabaseUserId()
        )
    }

    suspend fun getOutgoingFriendRequests(): List<FriendshipDto> {
        return friendshipRemoteDataSource.fetchOutgoingRequests(
            userId = requireSupabaseUserId()
        )
    }

    suspend fun sendFriendRequest(
        addresseeId: String
    ) {
        friendshipRemoteDataSource.sendFriendRequest(
            currentUserId = requireSupabaseUserId(),
            friendUserId = addresseeId
        )
    }

    suspend fun acceptFriendRequest(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.acceptFriendRequest(friendshipId)
    }

    suspend fun declineFriendRequest(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.declineFriendRequest(friendshipId)
    }

    suspend fun deleteFriendship(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.deleteFriendship(friendshipId)
    }

    private suspend fun fetchRemoteUserData(
        supabaseUserId: String
    ): RemoteUserData {
        val profile =
            profileRemoteDataSource.fetchProfile(supabaseUserId)

        val collectedCatIds =
            catRemoteDataSource.fetchCollectedCatIds(supabaseUserId)
                .distinct()

        val selectedCatIds =
            catRemoteDataSource.fetchSelectedCatIds(supabaseUserId)
                .distinct()

        val syncState =
            profileRemoteDataSource.fetchUserSyncState(supabaseUserId)

        return RemoteUserData(
            profile = profile,
            collectedCatIds = collectedCatIds,
            selectedCatIds = selectedCatIds,
            remoteUpdatedAt = syncState.remoteUpdatedAt
        )
    }

    private suspend fun uploadLocalUserToSupabase(
        localUser: User,
        supabaseUserId: String
    ) {
        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = localUser.username
        )

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = localUser.profileImageUrl?.toString()
        )

        catRemoteDataSource.uploadLocalCollectedCats(
            userId = supabaseUserId,
            catIds = localUser.collectedCatsIds
        )

        catRemoteDataSource.replaceSelectedCats(
            userId = supabaseUserId,
            selectedCatIds = localUser.selectedCatIds
        )
    }

    private suspend fun applyRemoteUserToLocal(
        localUser: User,
        supabaseUserId: String,
        remoteData: RemoteUserData
    ) {
        val remoteUser = localUser.copy(
            username = remoteData.profile.username,
            profileImageUrl = remoteData.profile.avatarPath?.let {
                java.net.URL(it)
            },
            collectedCatsIds = remoteData.collectedCatIds,
            selectedCatIds = remoteData.selectedCatIds,
            isSupabaseLinked = true,
            supabaseUserId = supabaseUserId
        )

        userRepository.updateUserFromRemoteSync(remoteUser)
    }

    private suspend fun requireLocalUser(): User {
        return userRepository.getUser(localUserId).firstOrNull()
            ?: error("Local user $localUserId does not exist.")
    }

    private fun requireSupabaseUserId(): String {
        return currentSupabaseUserId()
            ?: error("No authenticated Supabase user.")
    }

    private fun parseSupabaseTimestamp(value: String): Instant {
        return runCatching {
            Instant.parse(value)
        }.getOrElse {
            OffsetDateTime.parse(value).toInstant()
        }
    }

    private data class RemoteUserData(
        val profile: ProfileDto,
        val collectedCatIds: List<String>,
        val selectedCatIds: List<String>,
        val remoteUpdatedAt: String
    )

    private data class SyncComparableUserData(
        val username: String,
        val profileImageUrl: String?,
        val collectedCatIds: List<String>,
        val selectedCatIds: List<String>
    ) {
        companion object {
            fun fromLocal(user: User): SyncComparableUserData {
                return SyncComparableUserData(
                    username = user.username,
                    profileImageUrl = user.profileImageUrl?.toString(),
                    collectedCatIds = user.collectedCatsIds.sorted(),
                    selectedCatIds = user.selectedCatIds
                )
            }

            fun fromRemote(remoteData: RemoteUserData): SyncComparableUserData {
                return SyncComparableUserData(
                    username = remoteData.profile.username,
                    profileImageUrl = remoteData.profile.avatarPath,
                    collectedCatIds = remoteData.collectedCatIds.sorted(),
                    selectedCatIds = remoteData.selectedCatIds
                )
            }
        }
    }
}