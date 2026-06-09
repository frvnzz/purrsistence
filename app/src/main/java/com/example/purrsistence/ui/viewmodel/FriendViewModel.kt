package com.example.purrsistence.ui.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.FriendProfileDetails
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.domain.time.WeekWindowProvider
import com.example.purrsistence.service.TrackingSyncService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendViewModel(
    private val trackingSyncService: TrackingSyncService,
    private val weekWindowProvider: WeekWindowProvider
) : ViewModel() {

    private val _friends =
        MutableStateFlow<List<FriendProfile>>(emptyList())
    val friends: StateFlow<List<FriendProfile>> = _friends

    private val _incomingRequests =
        MutableStateFlow<List<Friendship>>(emptyList())
    val incomingRequests: StateFlow<List<Friendship>> = _incomingRequests

    private val _outgoingRequests =
        MutableStateFlow<List<Friendship>>(emptyList())
    val outgoingRequests: StateFlow<List<Friendship>> = _outgoingRequests

    private val _searchResults =
        MutableStateFlow<List<FriendProfile>>(emptyList())
    val searchResults: StateFlow<List<FriendProfile>> = _searchResults

    private val _selectedFriendProfile =
        MutableStateFlow<FriendProfileDetails?>(null)
    val selectedFriendProfile: StateFlow<FriendProfileDetails?> =
        _selectedFriendProfile

    private val _isLoading =
        MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadFriendsData() {
        viewModelScope.launch {
            loadFriendsDataInternal(showLoading = true)
        }
    }

    fun searchProfiles(query: String) {
        val trimmedQuery = query.trim()

        if (trimmedQuery.length < 2) {
            _searchResults.value = emptyList()
            _error.value = null
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (!trackingSyncService.isSignedIn()) {
                    _searchResults.value = emptyList()
                    _error.value = "Please sign in to search for friends."
                    return@launch
                }

                val currentUserId =
                    trackingSyncService.currentSupabaseUserId()

                val results =
                    trackingSyncService.searchProfiles(trimmedQuery)

                val existingFriendIds =
                    _friends.value.map { friend -> friend.id }.toSet()

                val outgoingRequestIds =
                    _outgoingRequests.value
                        .map { request -> request.addresseeId }
                        .toSet()

                _searchResults.value = results.filter { profile ->
                    profile.id != currentUserId &&
                            profile.id !in existingFriendIds &&
                            profile.id !in outgoingRequestIds
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _searchResults.value = emptyList()
                handleFriendError("searching for friends", exception)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFriendProfile(friendUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (!trackingSyncService.isSignedIn()) {
                    _selectedFriendProfile.value = null
                    return@launch
                }

                val weekWindow = weekWindowProvider.currentWeek()

                _selectedFriendProfile.value =
                    trackingSyncService.getFriendProfileDetails(
                        friendUserId = friendUserId,
                        weekStart = weekWindow.start,
                        weekEnd = weekWindow.end
                    )
            } catch (exception: Exception) {
                _error.value = exception.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedFriendProfile() {
        _selectedFriendProfile.value = null
    }

    fun sendFriendRequest(addresseeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackingSyncService.sendFriendRequest(addresseeId)

                loadFriendsDataInternal(showLoading = false)

                _searchResults.value =
                    _searchResults.value.filter { profile ->
                        profile.id != addresseeId
                    }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleFriendError("sending the friend request", exception)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptFriendRequest(friendshipId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackingSyncService.acceptFriendRequest(friendshipId)
                loadFriendsDataInternal(showLoading = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleFriendError("accepting the friend request", exception)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun declineFriendRequest(friendshipId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackingSyncService.declineFriendRequest(friendshipId)
                loadFriendsDataInternal(showLoading = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleFriendError("declining the friend request", exception)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFriendship(friendshipId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackingSyncService.deleteFriendship(friendshipId)
                loadFriendsDataInternal(showLoading = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleFriendError("removing the friend", exception)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun loadFriendsDataInternal(
        showLoading: Boolean
    ) {
        if (showLoading) {
            _isLoading.value = true
        }

        _error.value = null

        try {
            if (!trackingSyncService.isSignedIn()) {
                _friends.value = emptyList()
                _incomingRequests.value = emptyList()
                _outgoingRequests.value = emptyList()
                _error.value = "Please sign in to use friends."
                return
            }

            _friends.value =
                trackingSyncService.getFriends()

            _incomingRequests.value =
                trackingSyncService.getIncomingFriendRequests()

            _outgoingRequests.value =
                trackingSyncService.getOutgoingFriendRequests()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            handleFriendError("loading friends", exception)
        } finally {
            if (showLoading) {
                _isLoading.value = false
            }
        }
    }

    private fun friendlyFriendError(
        action: String,
        exception: Exception
    ): String {
        val rawMessage = exception.message.orEmpty()

        return when {
            rawMessage.contains("timeout", ignoreCase = true) ||
                    rawMessage.contains("request timeout", ignoreCase = true) ||
                    rawMessage.contains("failed to connect", ignoreCase = true) ||
                    rawMessage.contains("unable to resolve host", ignoreCase = true) ||
                    rawMessage.contains("no address associated with hostname", ignoreCase = true) ||
                    rawMessage.contains("network", ignoreCase = true) -> {
                "Something went wrong while $action. Please check your connection and try again."
            }

            rawMessage.contains("not authenticated", ignoreCase = true) ||
                    rawMessage.contains("No authenticated Supabase user", ignoreCase = true) -> {
                "Please sign in to use friends."
            }

            rawMessage.contains("already exists", ignoreCase = true) ||
                    rawMessage.contains("duplicate", ignoreCase = true) -> {
                "A friend request already exists."
            }

            else -> {
                "Something went wrong while $action. Please try again."
            }
        }
    }

    private fun handleFriendError(
        action: String,
        exception: Exception
    ) {
        Log.e("FriendViewModel", "Friend error while $action", exception)
        _error.value = friendlyFriendError(action, exception)
    }
}