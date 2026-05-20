package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.service.TrackingSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendViewModel(
    private val supabaseSyncService: TrackingSyncService
) : ViewModel() {

    private val _friends = MutableStateFlow<List<FriendProfile>>(emptyList())
    val friends: StateFlow<List<FriendProfile>> = _friends

    private val _incomingRequests = MutableStateFlow<List<Friendship>>(emptyList())
    val incomingRequests: StateFlow<List<Friendship>> = _incomingRequests

    private val _outgoingRequests = MutableStateFlow<List<Friendship>>(emptyList())
    val outgoingRequests: StateFlow<List<Friendship>> = _outgoingRequests

    private val _searchResults = MutableStateFlow<List<FriendProfile>>(emptyList())
    val searchResults: StateFlow<List<FriendProfile>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadFriendsData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (supabaseSyncService.isSignedIn()) {
                    _friends.value = supabaseSyncService.getFriends()
                    _incomingRequests.value = supabaseSyncService.getIncomingFriendRequests()
                    _outgoingRequests.value = supabaseSyncService.getOutgoingFriendRequests()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProfiles(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUserId = supabaseSyncService.currentSupabaseUserId()
                val results = supabaseSyncService.searchProfiles(query)
                // Filter out the current user and existing friends
                val filteredResults = results.filter { profile ->
                    profile.id != currentUserId && _friends.value.none { it.id == profile.id }
                }
                _searchResults.value = filteredResults
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendFriendRequest(addresseeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseSyncService.sendFriendRequest(addresseeId)
                loadFriendsData() //refresh outgoing requests
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptFriendRequest(friendshipId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseSyncService.acceptFriendRequest(friendshipId)
                loadFriendsData() //refresh friends and incoming requests
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun declineFriendRequest(friendshipId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseSyncService.declineFriendRequest(friendshipId)
                loadFriendsData() //refresh incoming requests
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFriendship(friendshipId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseSyncService.deleteFriendship(friendshipId)
                loadFriendsData() //refresh friends
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
