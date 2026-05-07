package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.service.ProfileService
import com.example.purrsistence.service.ShopService
import com.example.purrsistence.service.SupabaseSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val shopService: ShopService,
    private val supabaseSyncService: SupabaseSyncService,
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

    private val _supabaseError = MutableStateFlow<String?>(null)
    val supabaseError: StateFlow<String?> = _supabaseError

    private val _isSupabaseLoading = MutableStateFlow(false)
    val isSupabaseLoading: StateFlow<Boolean> = _isSupabaseLoading

    fun buyCat(catId: String, price: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Local purchase remains the primary app behavior:
                // balance deduction + local collectedCatsIds update.
                shopService.buyCat(currentUserId, catId, price)

                // If the user is signed in to Supabase, mirror the cat remotely.
                // If not signed in, the local app still works.
                if (supabaseSyncService.currentSupabaseUserId() != null) {
                    supabaseSyncService.uploadLocalCatsToSupabase()
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
            } catch (exception: Exception) {
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
                    supabaseSyncService.syncEverythingFromSupabase()
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

    fun clearSupabaseError() {
        _supabaseError.value = null
    }
}
