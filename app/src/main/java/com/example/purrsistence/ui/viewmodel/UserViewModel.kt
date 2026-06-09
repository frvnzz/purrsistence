package com.example.purrsistence.ui.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.domain.model.types.SyncStatus
import com.example.purrsistence.service.ProfileService
import com.example.purrsistence.service.ShopService
import com.example.purrsistence.service.TrackingSyncService
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val shopService: ShopService,
    private val supabaseSyncService: TrackingSyncService,
    private val profileService: ProfileService? = null,
    private val sharedPreferences: SharedPreferences? = null
) : ViewModel() {

    private val _tutorialCompleted = MutableStateFlow(
        sharedPreferences?.getBoolean("tutorial_completed", false) ?: true
    )
    val tutorialCompleted: StateFlow<Boolean> = _tutorialCompleted.asStateFlow()

    private val _tutorialStepIndex = MutableStateFlow(0)
    val tutorialStepIndex: StateFlow<Int> = _tutorialStepIndex.asStateFlow()

    fun nextTutorialStep() {
        _tutorialStepIndex.value += 1
    }

    fun completeTutorial() {
        sharedPreferences?.edit {
            putBoolean("tutorial_completed", true)
        }
        _tutorialCompleted.value = true
    }

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
    val isSupabaseSignedIn: StateFlow<Boolean> = supabaseSyncService.sessionStatus
        .map { it is SessionStatus.Authenticated }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            supabaseSyncService.isSignedIn()
        )

    // refresh the state of the remote user authorization (for session recovery)
    fun refreshAuthState() {
        // No longer needed as we observe sessionStatus flow
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
        viewModelScope.launch {
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
                handleSupabaseError(exception, "buying this cat")
            }
        }
    }

    fun updateSelectedCats(selectedIds: List<String>) {
        viewModelScope.launch {
            _supabaseError.value = null

            val result = shopService.updateSelectedCats(
                userId = currentUserId,
                selectedIds = selectedIds
            )

            if (result == SyncStatus.SYNC_FAILED) {
                _supabaseError.value =
                    "Cats were saved on this device, but syncing failed. Please check your connection and try again later."
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            val trimmedUsername = newUsername.trim()

            try {
                profileService?.updateProfile(
                    userId = currentUserId,
                    username = trimmedUsername,
                    profileImageUrl = user.value?.profileImageUrl
                )

                if (supabaseSyncService.currentSupabaseUserId() != null) {
                    supabaseSyncService.updateUsername(trimmedUsername)
                }
            } catch (exception: Exception) {
                handleSupabaseError(exception, "updating your username")
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun updateProfileImage(imageUrl: String?) {
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                profileService?.updateProfilePicture(
                    userId = currentUserId,
                    profileImageUrl = imageUrl
                )

                if (supabaseSyncService.currentSupabaseUserId() != null) {
                    supabaseSyncService.updateAvatarPath(imageUrl)
                }
            } catch (exception: Exception) {
                handleSupabaseError(exception, "updating your profile picture")
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun signUpWithSupabase(
        email: String,
        password: String,
        username: String
    ) {
        viewModelScope.launch {
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
                handleSupabaseError(exception, "creating your account")
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun signInWithSupabase(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.signIn(
                    email = email,
                    password = password
                )

            } catch (exception: Exception) {
                handleSupabaseError(exception, "signing in")

            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun signOutFromSupabase() {
        viewModelScope.launch {
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
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                if (supabaseSyncService.currentSupabaseUserId() != null) {
                    supabaseSyncService.checkAndSyncIfNeeded()
                }
            } catch (exception: Exception) {
                handleSupabaseError(exception, "syncing your data")
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun updateUsernameInSupabase(username: String) {
        viewModelScope.launch {
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

    fun updatePasswordInSupabase(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.updatePassword(currentPassword, newPassword)
            } catch (exception: Exception) {
                _supabaseError.value = exception.message
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun updateAvatarPathInSupabase(avatarPath: String?) {
        viewModelScope.launch{
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
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            _supabaseError.value = null

            try {
                supabaseSyncService.resetTrackingSessions(currentUserId)
            } catch (exception: Exception) {
                handleSupabaseError(exception, "resetting your tracking sessions")
            } finally {
                _isSupabaseLoading.value = false
            }
        }
    }

    fun clearSupabaseError() {
        _supabaseError.value = null
    }

    private fun friendlySupabaseError(
        exception: Exception,
        action: String
    ): String {
        val rawMessage = exception.message.orEmpty()

        return when {
            rawMessage.contains("Invalid login credentials", ignoreCase = true) -> {
                "The email or password is incorrect. Please check your details and try again."
            }

            rawMessage.contains("already registered", ignoreCase = true) ||
                    rawMessage.contains("already exists", ignoreCase = true) -> {
                "An account with this email already exists. Please log in instead."
            }

            rawMessage.contains("timeout", ignoreCase = true) ||
                    rawMessage.contains("request timeout", ignoreCase = true) ||
                    rawMessage.contains("failed to connect", ignoreCase = true) ||
                    rawMessage.contains("unable to resolve host", ignoreCase = true) ||
                    rawMessage.contains("network", ignoreCase = true) -> {
                "Something went wrong while $action. Please check your connection and try again. If the problem persists, contact support."
            }

            else -> {
                "Something went wrong while $action. Please try again. If the problem persists, contact support."
            }
        }
    }

    private fun handleSupabaseError(
        exception: Exception,
        action: String
    ) {
        Log.e("UserViewModel", "Supabase error while $action", exception)
        _supabaseError.value = friendlySupabaseError(exception, action)
    }
}
