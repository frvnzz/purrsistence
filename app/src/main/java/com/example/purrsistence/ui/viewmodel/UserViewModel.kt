package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.service.ProfileService
import com.example.purrsistence.service.ShopService
import com.example.purrsistence.service.TrackingSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val shopService: ShopService,
    private val supabaseSyncService: TrackingSyncService,
    private val profileService: ProfileService? = null
) : ViewModel() {

    // Centralized source of truth for the current local user
    val currentUserId: Int = 1

    val user = shopService
        .getUser(currentUserId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    // Centralized source of truth of remote user (sign up / in / out)
    private val _isSupabaseSignedIn =
        MutableStateFlow(
            supabaseSyncService.isSignedIn()
        )
    val isSupabaseSignedIn: StateFlow<Boolean> =
        _isSupabaseSignedIn
    // refresh the state of the remote user authorization (for session recovery)
    fun refreshAuthState() {
        _isSupabaseSignedIn.value =
            supabaseSyncService.isSignedIn()
    }

    private val _supabaseError = MutableStateFlow<String?>(null)
    val supabaseError: StateFlow<String?> = _supabaseError

    private val _isSupabaseLoading = MutableStateFlow(false)
    val isSupabaseLoading: StateFlow<Boolean> = _isSupabaseLoading

    // Check if account creation was a success so user can log in safely
    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess
    // used to reset the state after redirecting to login page
    fun resetSignUpSuccess() {
        _signUpSuccess.value = false
    }

    fun buyCat(catId: String, price: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Local purchase remains the primary app
                // behavior:
                // balance deduction + local collectedCatsIds update.
                shopService.buyCat(currentUserId, catId, price)

                // If the user is signed in to Supabase, mirror the cat remotely.
                // If not signed in, the local app still works.
                if (supabaseSyncService.currentSupabaseUserId() != null) {
                    supabaseSyncService.forceUploadLocalToSupabase()
                }
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            }
        }
    }

    fun updateSelectedCats(selectedIds: List<String>) {
        viewModelScope.launch {
            shopService.updateSelectedCats(currentUserId, selectedIds)
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            profileService?.updateProfile(
                userId = currentUserId,
                username = newUsername,
                profileImageUrl = user.value?.profileImageUrl
            )
        }
    }

    fun updateProfileImage(imageUrl: String?) {
        viewModelScope.launch {
            profileService?.updateProfilePicture(
                userId = currentUserId,
                profileImageUrl = imageUrl
            )
        }
    }

    fun signUpWithSupabase(
        email: String,
        password: String,
        username: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.signUp(
                    email = email,
                    password = password,
                    username = username
                )
                // set to false because user is not logged in yet
                _signUpSuccess.value = true
            } catch (exception: Exception) {
                _signUpSuccess.value = false
                _supabaseError.value = exception.message
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun signInWithSupabase(
        email: String,
        password: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.signIn(
                    email = email,
                    password = password
                )
                refreshAuthState()

            } catch (exception: Exception) {
                _supabaseError.value = exception.message

            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun signOutFromSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.signOut()
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            } finally {
                refreshAuthState()
                _isSupabaseLoading.value = false
            }
        }
    }

    fun syncFromSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                if (supabaseSyncService.currentSupabaseUserId() != null) {
                    supabaseSyncService.checkAndSyncIfNeeded()
                }
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun updateUsernameInSupabase(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.updateUsername(username)
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun updateAvatarPathInSupabase(avatarPath: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.updateAvatarPath(avatarPath)
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun resetTrackingSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.resetTrackingSessions(currentUserId)
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun clearSupabaseError() {
        _supabaseError.value = null
    }
}
