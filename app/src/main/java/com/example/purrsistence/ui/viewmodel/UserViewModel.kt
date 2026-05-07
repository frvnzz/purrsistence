package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.service.ProfileService
import com.example.purrsistence.service.ShopService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val shopService: ShopService,
    private val profileService: ProfileService? = null
) : ViewModel() {

    // Centralized source of truth for the current user
    val currentUserId: Int = 1

    val user = shopService
        .getUser(currentUserId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    fun buyCat(catId: String, price: Int) {
        viewModelScope.launch {
            shopService.buyCat(currentUserId, catId, price)
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
}