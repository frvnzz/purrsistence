package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.domain.time.TimeProvider
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.SupabaseSyncService
import com.example.purrsistence.service.TrackingService

class TrackingViewModelFactory(
    private val trackingService: TrackingService,
    private val rewardService: RewardService,
    private val timeProvider: TimeProvider,
    private val focusBlocker: FocusBlocker,
    private val supabaseSyncService: SupabaseSyncService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            return TrackingViewModel(
                trackingService = trackingService,
                rewardService = rewardService,
                timeProvider = timeProvider,
                focusBlocker = focusBlocker,
                supabaseSyncService
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}