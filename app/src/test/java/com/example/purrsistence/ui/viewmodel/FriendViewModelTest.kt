package com.example.purrsistence.ui.viewmodel

import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.FriendProfileDetails
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.domain.model.types.FriendshipStatus
import com.example.purrsistence.domain.service.fakes.FakeFriendSupabaseSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFriendsData_whenSignedIn_loadsFriendsAndRequests() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            friendsResult += FriendProfile(
                id = "friend-1",
                username = "alice"
            )

            incomingRequestsResult += Friendship(
                id = 1,
                requesterId = "bob-id",
                addresseeId = "current-user-id",
                status = FriendshipStatus.PENDING
            )

            outgoingRequestsResult += Friendship(
                id = 2,
                requesterId = "current-user-id",
                addresseeId = "charlie-id",
                status = FriendshipStatus.PENDING
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.loadFriendsData()
        runCurrent()

        assertEquals(1, syncService.getFriendsCalls)
        assertEquals(1, syncService.getIncomingRequestsCalls)
        assertEquals(1, syncService.getOutgoingRequestsCalls)

        assertEquals(listOf("alice"), viewModel.friends.value.map { it.username })
        assertEquals(listOf(1L), viewModel.incomingRequests.value.map { it.id })
        assertEquals(listOf(2L), viewModel.outgoingRequests.value.map { it.id })
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun loadFriendsData_whenSignedOut_clearsFriendsAndRequests() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            signedIn = false

            friendsResult += FriendProfile(
                id = "friend-1",
                username = "alice"
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.loadFriendsData()
        runCurrent()

        assertEquals(emptyList<FriendProfile>(), viewModel.friends.value)
        assertEquals(emptyList<Friendship>(), viewModel.incomingRequests.value)
        assertEquals(emptyList<Friendship>(), viewModel.outgoingRequests.value)
        assertEquals(0, syncService.getFriendsCalls)
    }

    @Test
    fun searchProfiles_withShortQuery_doesNotCallServiceAndClearsResults() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            searchResults += FriendProfile(
                id = "result-1",
                username = "alice"
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.searchProfiles("a")
        runCurrent()

        assertEquals(0, syncService.searchProfilesCalls)
        assertEquals(emptyList<FriendProfile>(), viewModel.searchResults.value)
    }

    @Test
    fun searchProfiles_filtersCurrentUserExistingFriendsAndOutgoingRequests() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            currentUserId = "current-user-id"

            friendsResult += FriendProfile(
                id = "existing-friend-id",
                username = "existing"
            )

            outgoingRequestsResult += Friendship(
                id = 4,
                requesterId = "current-user-id",
                addresseeId = "already-requested-id",
                status = FriendshipStatus.PENDING
            )

            searchResults += FriendProfile(
                id = "current-user-id",
                username = "me"
            )

            searchResults += FriendProfile(
                id = "existing-friend-id",
                username = "existing"
            )

            searchResults += FriendProfile(
                id = "already-requested-id",
                username = "requested"
            )

            searchResults += FriendProfile(
                id = "new-user-id",
                username = "newuser"
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.loadFriendsData()
        runCurrent()

        viewModel.searchProfiles("new")
        runCurrent()

        assertEquals(1, syncService.searchProfilesCalls)
        assertEquals("new", syncService.lastSearchQuery)

        assertEquals(
            listOf("newuser"),
            viewModel.searchResults.value.map { it.username }
        )
    }

    @Test
    fun sendFriendRequest_callsService_refreshesDataAndRemovesSearchResult() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            searchResults += FriendProfile(
                id = "target-user-id",
                username = "target"
            )

            outgoingRequestsResult += Friendship(
                id = 10,
                requesterId = "current-user-id",
                addresseeId = "target-user-id",
                status = FriendshipStatus.PENDING
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.searchProfiles("target")
        runCurrent()

        assertEquals(1, viewModel.searchResults.value.size)

        viewModel.sendFriendRequest("target-user-id")
        runCurrent()

        assertEquals(1, syncService.sendFriendRequestCalls)
        assertEquals("target-user-id", syncService.lastAddresseeId)
        assertEquals(emptyList<FriendProfile>(), viewModel.searchResults.value)
        assertEquals(1, viewModel.outgoingRequests.value.size)
        assertNull(viewModel.error.value)
    }

    @Test
    fun acceptFriendRequest_callsServiceAndRefreshesFriendsData() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            friendsResult += FriendProfile(
                id = "new-friend-id",
                username = "newfriend"
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.acceptFriendRequest(99)
        runCurrent()

        assertEquals(1, syncService.acceptFriendRequestCalls)
        assertEquals(99L, syncService.lastAcceptedFriendshipId)
        assertEquals(listOf("newfriend"), viewModel.friends.value.map { it.username })
    }

    @Test
    fun declineFriendRequest_callsServiceAndRefreshesFriendsData() = runTest {
        val syncService = FakeFriendSupabaseSyncService()

        val viewModel = FriendViewModel(syncService)

        viewModel.declineFriendRequest(88)
        runCurrent()

        assertEquals(1, syncService.declineFriendRequestCalls)
        assertEquals(88L, syncService.lastDeclinedFriendshipId)
        assertEquals(1, syncService.getFriendsCalls)
        assertEquals(1, syncService.getIncomingRequestsCalls)
        assertEquals(1, syncService.getOutgoingRequestsCalls)
    }

    @Test
    fun deleteFriendship_callsServiceAndRefreshesFriendsData() = runTest {
        val syncService = FakeFriendSupabaseSyncService()

        val viewModel = FriendViewModel(syncService)

        viewModel.deleteFriendship(77)
        runCurrent()

        assertEquals(1, syncService.deleteFriendshipCalls)
        assertEquals(77L, syncService.lastDeletedFriendshipId)
        assertEquals(1, syncService.getFriendsCalls)
        assertEquals(1, syncService.getIncomingRequestsCalls)
        assertEquals(1, syncService.getOutgoingRequestsCalls)
    }

    @Test
    fun loadFriendProfile_loadsFriendProfileDetails() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            friendProfileDetailsResult = FriendProfileDetails(
                profile = FriendProfile(
                    id = "friend-id",
                    username = "alice"
                ),
                collectedCatIds = listOf("cat_1", "cat_2"),
                selectedCatIds = listOf("cat_1")
            )
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.loadFriendProfile("friend-id")
        runCurrent()

        assertEquals(1, syncService.getFriendProfileDetailsCalls)
        assertEquals("friend-id", syncService.lastLoadedFriendUserId)

        assertEquals(
            "alice",
            viewModel.selectedFriendProfile.value?.profile?.username
        )

        assertEquals(
            listOf("cat_1", "cat_2"),
            viewModel.selectedFriendProfile.value?.collectedCatIds
        )

        assertEquals(
            listOf("cat_1"),
            viewModel.selectedFriendProfile.value?.selectedCatIds
        )
    }

    @Test
    fun clearSelectedFriendProfile_setsSelectedFriendProfileToNull() = runTest {
        val syncService = FakeFriendSupabaseSyncService()

        val viewModel = FriendViewModel(syncService)

        viewModel.loadFriendProfile("friend-id")
        runCurrent()

        viewModel.clearSelectedFriendProfile()

        assertNull(viewModel.selectedFriendProfile.value)
    }

    @Test
    fun searchProfiles_whenServiceThrows_setsError() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            throwOnSearch = IllegalStateException("Search failed")
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.searchProfiles("alice")
        runCurrent()

        assertEquals("Search failed", viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun sendFriendRequest_whenServiceThrows_setsError() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            throwOnSendRequest = IllegalStateException("Request failed")
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.sendFriendRequest("target-user-id")
        runCurrent()

        assertEquals("Request failed", viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun clearError_setsErrorToNull() = runTest {
        val syncService = FakeFriendSupabaseSyncService().apply {
            throwOnSearch = IllegalStateException("Search failed")
        }

        val viewModel = FriendViewModel(syncService)

        viewModel.searchProfiles("alice")
        runCurrent()

        assertEquals("Search failed", viewModel.error.value)

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }
}