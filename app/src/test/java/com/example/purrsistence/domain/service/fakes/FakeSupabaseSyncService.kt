package com.example.purrsistence.domain.service.fakes

import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Friendship
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
    var getFriendsCalls = 0
    var getIncomingFriendRequestsCalls = 0
    var getOutgoingFriendRequestsCalls = 0
    var sendFriendRequestCalls = 0
    var acceptFriendRequestCalls = 0
    var declineFriendRequestCalls = 0
    var deleteFriendshipCalls = 0

    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastUsername: String? = null
    var lastCatId: String? = null
    var lastAvatarPath: String? = null
    var lastAddresseeId: String? = null
    var lastAcceptedFriendshipId: Long? = null
    var lastDeclinedFriendshipId: Long? = null
    var lastDeletedFriendshipId: Long? = null

    var syncAfterGoalChangedResultWhenSignedIn: SyncStatus = SyncStatus.IN_SYNC
    var syncAfterTrackingChangedResultWhenSignedIn: SyncStatus = SyncStatus.IN_SYNC
    var checkAndSyncResultWhenSignedIn: SyncStatus = SyncStatus.IN_SYNC
    var forceUploadResultWhenSignedIn: SyncStatus = SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
    var forceDownloadResultWhenSignedIn: SyncStatus = SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE

    var friends: List<FriendProfile> = emptyList()
    var incomingFriendRequests: List<Friendship> = emptyList()
    var outgoingFriendRequests: List<Friendship> = emptyList()

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
            syncAfterGoalChangedResultWhenSignedIn
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun syncAfterLocalTrackingSessionChanged(): SyncStatus {
        syncAfterTrackingChangedCalls++

        return if (signedIn) {
            syncAfterTrackingChangedResultWhenSignedIn
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun checkAndSyncIfNeeded(): SyncStatus {
        checkAndSyncCalls++

        return if (signedIn) {
            checkAndSyncResultWhenSignedIn
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun forceUploadLocalToSupabase(): SyncStatus {
        forceUploadCalls++

        return if (signedIn) {
            forceUploadResultWhenSignedIn
        } else {
            SyncStatus.NOT_LINKED
        }
    }

    override suspend fun forceDownloadFromSupabase(): SyncStatus {
        forceDownloadCalls++

        return if (signedIn) {
            forceDownloadResultWhenSignedIn
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

    override suspend fun getFriends(): List<FriendProfile> {
        getFriendsCalls++
        return friends
    }

    override suspend fun getIncomingFriendRequests(): List<Friendship> {
        getIncomingFriendRequestsCalls++
        return incomingFriendRequests
    }

    override suspend fun getOutgoingFriendRequests(): List<Friendship> {
        getOutgoingFriendRequestsCalls++
        return outgoingFriendRequests
    }

    override suspend fun sendFriendRequest(
        addresseeId: String
    ) {
        sendFriendRequestCalls++
        lastAddresseeId = addresseeId
    }

    override suspend fun acceptFriendRequest(
        friendshipId: Long
    ) {
        acceptFriendRequestCalls++
        lastAcceptedFriendshipId = friendshipId
    }

    override suspend fun declineFriendRequest(
        friendshipId: Long
    ) {
        declineFriendRequestCalls++
        lastDeclinedFriendshipId = friendshipId
    }

    override suspend fun deleteFriendship(
        friendshipId: Long
    ) {
        deleteFriendshipCalls++
        lastDeletedFriendshipId = friendshipId
    }
}