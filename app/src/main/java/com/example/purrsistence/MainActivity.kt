package com.example.purrsistence

import android.content.Intent
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
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.viewmodel.StatisticsViewModelFactory
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.TrackingViewModelFactory
import com.example.purrsistence.ui.viewmodel.UserViewModel
import com.example.purrsistence.ui.viewmodel.FriendViewModel
import com.example.purrsistence.ui.viewmodel.FriendViewModelFactory
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

        userViewModel = UserViewModel(
            shopService = appContainer.shopService,
            supabaseSyncService = appContainer.supabaseSyncService,
            profileService = appContainer.profileService
        )

        goalViewModel = GoalViewModel(
            goalService = appContainer.goalService,
            sharedPreferences = appContainer.focusPrefs,
            supabaseSyncService = appContainer.supabaseSyncService
        )

        trackingViewModel = ViewModelProvider(
            this,
            TrackingViewModelFactory(
                trackingService = appContainer.trackingService,
                rewardService = appContainer.rewardService,
                timeProvider = appContainer.timeProvider,
                focusBlocker = appContainer.focusBlocker,
                trackingNotificationController = appContainer.trackingNotificationController,
                sessionReminderScheduler = appContainer.sessionReminderScheduler,
                supabaseSyncService = appContainer.supabaseSyncService
            )
        )[TrackingViewModel::class.java]

        statisticsViewModel = ViewModelProvider(
            this,
            StatisticsViewModelFactory(appContainer.statisticsService)
        )[StatisticsViewModel::class.java]

        friendViewModel =
            ViewModelProvider(
                this,
                FriendViewModelFactory(
                    supabaseSyncService = appContainer.supabaseSyncService
                )
            )[FriendViewModel::class.java]

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