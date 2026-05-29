package com.example.purrsistence.domain.service.fakes

import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.FriendProfileDetails
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.domain.model.types.SyncStatus
import com.example.purrsistence.service.TrackingSyncService

class FakeFriendSupabaseSyncService : TrackingSyncService {

    var signedIn: Boolean = true
    var currentUserId: String? = "current-user-id"

    var signUpCalls = 0
    var signInCalls = 0
    var signOutCalls = 0
    var searchProfilesCalls = 0
    var getFriendsCalls = 0
    var getIncomingRequestsCalls = 0
    var getOutgoingRequestsCalls = 0
    var getFriendProfileDetailsCalls = 0
    var sendFriendRequestCalls = 0
    var acceptFriendRequestCalls = 0
    var declineFriendRequestCalls = 0
    var deleteFriendshipCalls = 0

    var lastSearchQuery: String? = null
    var lastAddresseeId: String? = null
    var lastAcceptedFriendshipId: Long? = null
    var lastDeclinedFriendshipId: Long? = null
    var lastDeletedFriendshipId: Long? = null
    var lastLoadedFriendUserId: String? = null

    val friendsResult = mutableListOf<FriendProfile>()
    val incomingRequestsResult = mutableListOf<Friendship>()
    val outgoingRequestsResult = mutableListOf<Friendship>()
    val searchResults = mutableListOf<FriendProfile>()

    var friendProfileDetailsResult: FriendProfileDetails =
        FriendProfileDetails(
            profile = FriendProfile(
                id = "friend-id",
                username = "friend"
            ),
            collectedCatIds = listOf("cat_1"),
            selectedCatIds = listOf("cat_1")
        )

    var throwOnSearch: Exception? = null
    var throwOnLoadFriends: Exception? = null
    var throwOnSendRequest: Exception? = null
    var throwOnAccept: Exception? = null
    var throwOnDecline: Exception? = null
    var throwOnDelete: Exception? = null
    var throwOnLoadFriendProfile: Exception? = null

    override fun isSignedIn(): Boolean {
        return signedIn
    }

    override fun currentSupabaseUserId(): String? {
        return currentUserId
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        signUpCalls++
        signedIn = true
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        signInCalls++
        signedIn = true
    }

    override suspend fun signOut() {
        signOutCalls++
        signedIn = false
        currentUserId = null
    }

    override suspend fun syncAfterLocalGoalChanged(): SyncStatus {
        return SyncStatus.IN_SYNC
    }

    override suspend fun syncAfterLocalTrackingSessionChanged(): SyncStatus {
        return SyncStatus.IN_SYNC
    }

    override suspend fun checkAndSyncIfNeeded(): SyncStatus {
        return SyncStatus.IN_SYNC
    }

    override suspend fun forceUploadLocalToSupabase(): SyncStatus {
        return SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
    }

    override suspend fun forceDownloadFromSupabase(): SyncStatus {
        return SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE
    }

    override suspend fun addCollectedCatToSupabaseAndLocal(catId: String) = Unit

    override suspend fun updateUsername(username: String) = Unit

    override suspend fun updateAvatarPath(avatarPath: String?) = Unit

    override suspend fun getFriends(): List<FriendProfile> {
        throwOnLoadFriends?.let { throw it }
        getFriendsCalls++
        return friendsResult.toList()
    }

    override suspend fun getIncomingFriendRequests(): List<Friendship> {
        getIncomingRequestsCalls++
        return incomingRequestsResult.toList()
    }

    override suspend fun getOutgoingFriendRequests(): List<Friendship> {
        getOutgoingRequestsCalls++
        return outgoingRequestsResult.toList()
    }

    override suspend fun searchProfiles(
        query: String
    ): List<FriendProfile> {
        throwOnSearch?.let { throw it }
        searchProfilesCalls++
        lastSearchQuery = query
        return searchResults.toList()
    }

    override suspend fun sendFriendRequest(
        addresseeId: String
    ) {
        throwOnSendRequest?.let { throw it }
        sendFriendRequestCalls++
        lastAddresseeId = addresseeId
    }

    override suspend fun acceptFriendRequest(
        friendshipId: Long
    ) {
        throwOnAccept?.let { throw it }
        acceptFriendRequestCalls++
        lastAcceptedFriendshipId = friendshipId
    }

    override suspend fun declineFriendRequest(
        friendshipId: Long
    ) {
        throwOnDecline?.let { throw it }
        declineFriendRequestCalls++
        lastDeclinedFriendshipId = friendshipId
    }

    override suspend fun getFriendProfileDetails(
        friendUserId: String
    ): FriendProfileDetails {
        throwOnLoadFriendProfile?.let { throw it }
        getFriendProfileDetailsCalls++
        lastLoadedFriendUserId = friendUserId
        return friendProfileDetailsResult
    }

    override suspend fun deleteFriendship(
        friendshipId: Long
    ) {
        throwOnDelete?.let { throw it }
        deleteFriendshipCalls++
        lastDeletedFriendshipId = friendshipId
    }
}