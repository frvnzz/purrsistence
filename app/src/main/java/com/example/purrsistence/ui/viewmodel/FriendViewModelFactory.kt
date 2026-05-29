package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.purrsistence.service.TrackingSyncService

class FriendViewModelFactory(
    private val supabaseSyncService: TrackingSyncService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendViewModel(supabaseSyncService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
