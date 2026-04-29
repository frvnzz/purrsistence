package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.service.ShopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val shopService: ShopService
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
        viewModelScope.launch(Dispatchers.IO) {
            shopService.buyCat(currentUserId, catId, price)
        }
    }

    fun updateSelectedCats(selectedIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            shopService.updateSelectedCats(currentUserId, selectedIds)
        }
    }
}