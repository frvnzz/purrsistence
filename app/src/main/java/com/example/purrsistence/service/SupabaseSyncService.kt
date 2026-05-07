package com.example.purrsistence.service

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseAuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseFriendshipRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseProfileRemoteDataSource
import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import kotlinx.coroutines.flow.firstOrNull

class SupabaseSyncService(
    private val userDao: UserDao,
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

        if (authRemoteDataSource.isSignedIn()) {
            syncEverythingFromSupabase()
            uploadLocalCatsToSupabase()
        }
    }

    suspend fun signIn(
        email: String,
        password: String
    ) {
        authRemoteDataSource.signIn(
            email = email,
            password = password
        )

        syncEverythingFromSupabase()
        uploadLocalCatsToSupabase()
    }

    suspend fun signOut() {
        authRemoteDataSource.signOut()
    }

    fun currentSupabaseUserId(): String? {
        return authRemoteDataSource.currentUserId()
    }

    suspend fun syncEverythingFromSupabase() {
        if (!isSignedIn()) return

        syncProfileFromSupabase()
        syncCollectedCatsFromSupabase()
    }

    suspend fun syncProfileFromSupabase() {
        val supabaseUserId = requireSupabaseUserId()
        val profile = profileRemoteDataSource.fetchProfile(supabaseUserId)
        val localUser = requireLocalUser()

        userDao.updateUser(
            localUser.copy(
                username = profile.username,
                profileImageUrl = profile.avatarPath,
                isSupabaseLinked = true,
                supabaseUserId = profile.id
            )
        )
    }

    suspend fun syncCollectedCatsFromSupabase() {
        val supabaseUserId = requireSupabaseUserId()
        val remoteCatIds = catRemoteDataSource
            .fetchCollectedCatIds(supabaseUserId)
            .distinct()

        val localUser = requireLocalUser()

        userDao.updateUser(
            localUser.copy(
                collectedCatsIds = remoteCatIds,
                isSupabaseLinked = true,
                supabaseUserId = supabaseUserId
            )
        )
    }

    suspend fun uploadLocalCatsToSupabase() {
        if (!isSignedIn()) return

        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        catRemoteDataSource.uploadLocalCollectedCats(
            userId = supabaseUserId,
            catIds = localUser.collectedCatsIds
        )
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

        val updatedCatIds =
            (localUser.collectedCatsIds + catId).distinct()

        userDao.updateUser(
            localUser.copy(
                collectedCatsIds = updatedCatIds,
                isSupabaseLinked = true,
                supabaseUserId = supabaseUserId
            )
        )
    }

    suspend fun updateUsername(
        username: String
    ) {
        val supabaseUserId = requireSupabaseUserId()

        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = username
        )

        val localUser = requireLocalUser()

        userDao.updateUser(
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

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = avatarPath
        )

        val localUser = requireLocalUser()

        userDao.updateUser(
            localUser.copy(
                profileImageUrl = avatarPath,
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

    private fun requireSupabaseUserId(): String {
        return authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")
    }

    private suspend fun requireLocalUser(): UserEntity {
        return userDao.getUser(localUserId).firstOrNull()
            ?: error("Local user $localUserId does not exist.")
    }
}