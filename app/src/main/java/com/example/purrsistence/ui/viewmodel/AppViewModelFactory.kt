package com.example.purrsistence.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.purrsistence.AppContainer

class AppViewModelFactory(
    private val appContainer: AppContainer,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        return when (modelClass) {
            UserViewModel::class.java -> {
                UserViewModel(
                    shopService = appContainer.shopService,
                    supabaseSyncService = appContainer.supabaseSyncService,
                    profileService = appContainer.profileService,
                    sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                )
            }
            GoalViewModel::class.java -> {
                GoalViewModel(
                    goalService = appContainer.goalService,
                    sharedPreferences = appContainer.focusPrefs,
                    supabaseSyncService = appContainer.supabaseSyncService
                )
            }
            TrackingViewModel::class.java -> {
                TrackingViewModel(
                    trackingService = appContainer.trackingService,
                    rewardService = appContainer.rewardService,
                    timeProvider = appContainer.timeProvider,
                    focusBlocker = appContainer.focusBlocker,
                    trackingNotificationController = appContainer.trackingNotificationController,
                    sessionReminderScheduler = appContainer.sessionReminderScheduler,
                    supabaseSyncService = appContainer.supabaseSyncService
                )
            }
            StatisticsViewModel::class.java -> {
                StatisticsViewModel(
                    statisticsService = appContainer.statisticsService,
                    savedStateHandle = savedStateHandle
                )
            }
            FriendViewModel::class.java -> {
                FriendViewModel(
                    trackingSyncService = appContainer.supabaseSyncService,
                    weekWindowProvider = appContainer.weekWindowProvider
                )
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}
