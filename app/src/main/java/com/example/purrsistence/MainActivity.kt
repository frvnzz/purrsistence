package com.example.purrsistence

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.notifications.ReminderNotificationManager
import com.example.purrsistence.notifications.TrackingNotificationManager
import com.example.purrsistence.service.TrackingForegroundService
import com.example.purrsistence.ui.screens.MainScreen
import com.example.purrsistence.ui.theme.PurrsistenceTheme
import com.example.purrsistence.ui.viewmodel.AppViewModelFactory
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import com.example.purrsistence.ui.viewmodel.FriendViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var trackingViewModel: TrackingViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var friendViewModel: FriendViewModel

    private var openTrackingFromNotification by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TrackingNotificationManager(this).createChannels()
        ReminderNotificationManager(this).createChannels()


        val appContainer = (application as PurrsistenceApplication).appContainer

        // Optional place for notification channel creation later, for example:
        // TrackingNotificationManager(this).createChannels()

        val factory = AppViewModelFactory(appContainer, this)

        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
        goalViewModel = ViewModelProvider(this, factory)[GoalViewModel::class.java]
        trackingViewModel = ViewModelProvider(this, factory)[TrackingViewModel::class.java]
        statisticsViewModel = ViewModelProvider(this, factory)[StatisticsViewModel::class.java]
        friendViewModel = ViewModelProvider(this, factory)[FriendViewModel::class.java]

        handleNotificationIntent(intent)

        lifecycleScope.launch {
            if (appContainer.userDao.getUser(1).firstOrNull() == null) {
                val exampleUserEntity = UserEntity(
                    userId = 1,
                    username = "testuser",
                    balance = 100,
                    friends = listOf("alice", "bob"),
                    collectedCatsIds = listOf("cat_1"),
                    selectedCatIds = listOf("cat_1"),
                    profileImageUrl = null,
                    isSupabaseLinked = false,
                    supabaseUserId = null
                )
                appContainer.userDao.insertUser(exampleUserEntity)
            }

            appContainer.cleanupScheduler.runIfDue()

            // Optional later:
            // if (appContainer.supabaseSyncService.isSignedIn()) {
            //     appContainer.supabaseSyncService.checkAndSyncIfNeeded()
            // }
        }

        setContent {
            PurrsistenceTheme {
                MainScreen(
                    userViewModel = userViewModel,
                    goalViewModel = goalViewModel,
                    trackingViewModel = trackingViewModel,
                    statisticsViewModel = statisticsViewModel,
                    openTrackingFromNotification = openTrackingFromNotification,
                    onTrackingNotificationHandled = {
                        openTrackingFromNotification = false
                    },
                    friendViewModel = friendViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == TrackingForegroundService.ACTION_OPEN_TRACKING_FROM_NOTIFICATION) {
            openTrackingFromNotification = true

            intent.action = null
            intent.removeExtra(TrackingForegroundService.EXTRA_TRACKING_ID)
            intent.removeExtra(TrackingForegroundService.EXTRA_GOAL_TITLE)
            intent.removeExtra(TrackingForegroundService.EXTRA_START_TIME_MILLIS)
        }
    }
}