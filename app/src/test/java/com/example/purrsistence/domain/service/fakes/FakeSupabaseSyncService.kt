package com.example.purrsistence.domain.service.fakes

import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import com.example.purrsistence.domain.model.types.SyncStatus
import com.example.purrsistence.service.TrackingSyncService

class FakeSupabaseSyncService(
    private var signedIn: Boolean = false,
    private var supabaseUserId: String? = null
) : TrackingSyncService {

    var signUpCalls = 0
    var signInCalls = 0
    var signOutCalls = 0
    var syncAfterGoalChangedCalls = 0
    var syncAfterTrackingChangedCalls = 0
    var checkAndSyncCalls = 0
    var forceUploadCalls = 0
    var forceDownloadCalls = 0
    var addCollectedCatCalls = 0
    var updateUsernameCalls = 0
    var updateAvatarPathCalls = 0

    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastUsername: String? = null
    var lastCatId: String? = null
    var lastAvatarPath: String? = null

    override fun isSignedIn(): Boolean {
        return signedIn
    }

    override fun currentSupabaseUserId(): String? {
        return supabaseUserId
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        signUpCalls++
        lastEmail = email
        lastPassword = password
        lastUsername = username

        signedIn = true
        supabaseUserId = "fake-supabase-user-id"
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        signInCalls++
        lastEmail = email
        lastPassword = password

        signedIn = true
        supabaseUserId = "fake-supabase-user-id"
    }

    override suspend fun signOut() {
        signOutCalls++
        signedIn = false
        supabaseUserId = null
    }

    override suspend fun syncAfterLocalGoalChanged(): SyncStatus {
        syncAfterGoalChangedCalls++
        return if (signedIn) {
            SyncStatus.IN_SYNC
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun syncAfterLocalTrackingSessionChanged(): SyncStatus {
        syncAfterTrackingChangedCalls++
        return if (signedIn) {
            SyncStatus.IN_SYNC
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun checkAndSyncIfNeeded(): SyncStatus {
        checkAndSyncCalls++
        return if (signedIn) {
            SyncStatus.IN_SYNC
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun forceUploadLocalToSupabase(): SyncStatus {
        forceUploadCalls++
        return if (signedIn) {
            SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun forceDownloadFromSupabase(): SyncStatus {
        forceDownloadCalls++
        return if (signedIn) {
            SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun addCollectedCatToSupabaseAndLocal(
        catId: String
    ) {
        addCollectedCatCalls++
        lastCatId = catId
    }

    override suspend fun updateUsername(
        username: String
    ) {
        updateUsernameCalls++
        lastUsername = username
    }

    override suspend fun updateAvatarPath(
        avatarPath: String?
    ) {
        updateAvatarPathCalls++
        lastAvatarPath = avatarPath
    }

    override suspend fun getFriends(): List<ProfileDto> {
        return emptyList()
    }

    override suspend fun getIncomingFriendRequests(): List<FriendshipDto> {
        return emptyList()
    }

    override suspend fun getOutgoingFriendRequests(): List<FriendshipDto> {
        return emptyList()
    }

    override suspend fun sendFriendRequest(
        addresseeId: String
    ) = Unit

    override suspend fun acceptFriendRequest(
        friendshipId: Long
    ) = Unit

    override suspend fun declineFriendRequest(
        friendshipId: Long
    ) = Unit

    override suspend fun deleteFriendship(
        friendshipId: Long
    ) = Unit
}