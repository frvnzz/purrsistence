package com.example.purrsistence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.purrsistence.data.focus.SharedPrefsFocusBlocker
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.GoalRepositoryImpl
import com.example.purrsistence.data.local.repository.StatisticsRepository
import com.example.purrsistence.data.local.repository.StatisticsRepositoryImpl
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.data.local.repository.UserRepositoryImpl
import com.example.purrsistence.data.remote.repository.ProfileRepository
import com.example.purrsistence.data.remote.supabase.SupabaseClientProvider
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseAuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseFriendshipRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseProfileRemoteDataSource
import com.example.purrsistence.domain.preferences.SharedPrefCleanupPreferences
import com.example.purrsistence.domain.time.SystemTimeProvider
import com.example.purrsistence.focus.DeepFocusConfig
import com.example.purrsistence.service.CleanupScheduler
import com.example.purrsistence.service.GoalService
import com.example.purrsistence.service.ProfileService
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.ShopService
import com.example.purrsistence.service.StatisticsService
import com.example.purrsistence.service.SupabaseSyncService
import com.example.purrsistence.service.TrackingCleanupService
import com.example.purrsistence.service.TrackingServiceImpl
import com.example.purrsistence.ui.screens.MainScreen
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.viewmodel.StatisticsViewModelFactory
import com.example.purrsistence.ui.theme.PurrsistenceTheme
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class MainActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var trackingViewModel: TrackingViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // DATABASE & DAO
        val db = AppDatabase.getInstance(this)
        val goalsDao = db.goalsDao()
        val trackingDao = db.trackingDao()
        val userDao = db.userDao()

        // REPOSITORIES
        val userRepo : UserRepository = UserRepositoryImpl(userDao)
        val goalRepo : GoalRepository = GoalRepositoryImpl(goalsDao) // replace with goalDao when it's implemented
        val timeProvider = SystemTimeProvider()
        val trackingRepo : TrackingRepository = TrackingRepositoryImpl(trackingDao)
        val statisticsRepo : StatisticsRepository= StatisticsRepositoryImpl(goalsDao, trackingDao)

        //Services
        val goalService = GoalService(goalRepo, timeProvider)
        val rewardService = RewardService()
        val trackingService = TrackingServiceImpl(trackingRepo, userRepo, goalRepo, goalService, rewardService, timeProvider)
        val shopService = ShopService(userRepo)
        val profileService = ProfileService(this, userRepo)
        val statisticsService = StatisticsService(statisticsRepo)
        val trackingCleanupService = TrackingCleanupService(goalRepo,trackingRepo, timeProvider)

        // shared preferences (for storing last selected goal from GoalBottomDrawer)
        val focusPrefs = getSharedPreferences(DeepFocusConfig.PREFS_NAME, MODE_PRIVATE)
        val focusBlocker = SharedPrefsFocusBlocker(focusPrefs)
        val cleanupPrefs = SharedPrefCleanupPreferences(getSharedPreferences("app_prefs", MODE_PRIVATE))

        val supabase = SupabaseClientProvider.create()

        val supabaseAuthRemoteDataSource =
            SupabaseAuthRemoteDataSource(supabase)

        val supabaseProfileRemoteDataSource =
            SupabaseProfileRemoteDataSource(supabase)

        val supabaseCatRemoteDataSource =
            SupabaseCatRemoteDataSource(supabase)

        val supabaseFriendshipRemoteDataSource =
            SupabaseFriendshipRemoteDataSource(supabase)

        val supabaseSyncService =
            SupabaseSyncService(
                userDao = userDao,
                authRemoteDataSource = supabaseAuthRemoteDataSource,
                profileRemoteDataSource = supabaseProfileRemoteDataSource,
                catRemoteDataSource = supabaseCatRemoteDataSource,
                friendshipRemoteDataSource = supabaseFriendshipRemoteDataSource,
                localUserId = 1
            )

        // create ViewModel instances for this activity
        userViewModel = UserViewModel(shopService, profileService)
        goalViewModel = GoalViewModel(goalService, focusPrefs)
        trackingViewModel = TrackingViewModel(trackingService, timeProvider, focusBlocker)
        // Use factory for StatisticsViewModel to preserve week offset across configuration changes
        statisticsViewModel = ViewModelProvider(
            this,
            StatisticsViewModelFactory(statisticsService)
        ).get(StatisticsViewModel::class.java)

        val cleanupScheduler = CleanupScheduler(cleanupPrefs, timeProvider, trackingCleanupService)

        lifecycleScope.launch {
            // Only insert if userId 1 doesn't exist
            if (userDao.getUser(1).firstOrNull() == null) {
                val exampleUserEntity = UserEntity(
                    userId = 1, // fixed userId 1 for the test user
                    username = "testuser",
                    balance = 100,
                    friends = listOf("alice", "bob"),
                    collectedCatsIds = listOf("cat_1"),
                    selectedCatIds = listOf("cat_1"),
                    profileImageUrl = null,
                    isSupabaseLinked = false,
                    supabaseUserId = null
                )
                userDao.insertUser(exampleUserEntity)
            }

            //reset completed goals for new time window/cycle
            //TODO: update somewhere else, because this is to infrequent and only happens when app is opened
            //goalService.resetCompletedGoalsIfNewCycle(
            //    userId = 1,
            //    now = ZonedDateTime.now()
            //)
            cleanupScheduler.runIfDue()
        }

        setContent {
            PurrsistenceTheme {
                // pass created ViewModels to MainScreen (scaffold)
                MainScreen(
                    userViewModel = userViewModel,
                    goalViewModel = goalViewModel,
                    trackingViewModel = trackingViewModel,
                    statisticsViewModel = statisticsViewModel,
                )
            }
        }
    }
}