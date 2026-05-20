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
import com.example.purrsistence.data.remote.supabase.SupabaseClientProvider
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseAuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseFriendshipRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseGoalTrackingRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseProfileRemoteDataSource
import com.example.purrsistence.data.remote.supabase.repository.AuthRepository
import com.example.purrsistence.data.remote.supabase.repository.AuthRepositoryImpl
import com.example.purrsistence.data.remote.supabase.repository.CatCollectionRepository
import com.example.purrsistence.data.remote.supabase.repository.CatCollectionRepositoryImpl
import com.example.purrsistence.data.remote.supabase.repository.FriendshipRepository
import com.example.purrsistence.data.remote.supabase.repository.FriendshipRepositoryImpl
import com.example.purrsistence.data.remote.supabase.repository.GoalTrackingRepository
import com.example.purrsistence.data.remote.supabase.repository.GoalTrackingRepositoryImpl
import com.example.purrsistence.data.remote.supabase.repository.ProfileRepository
import com.example.purrsistence.data.remote.supabase.repository.ProfileRepositoryImpl
import com.example.purrsistence.data.remote.supabase.repository.SyncSnapshotRepository
import com.example.purrsistence.data.remote.supabase.repository.SyncSnapshotRepositoryImpl
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // DATABASE & DAO
        val db = AppDatabase.getInstance(this)
        val goalsDao = db.goalsDao()
        val trackingDao = db.trackingDao()
        val userDao = db.userDao()

        // REPOSITORIES
        val timeProvider = SystemTimeProvider()
        val userRepo : UserRepository = UserRepositoryImpl(userDao, timeProvider)
        val goalRepo : GoalRepository = GoalRepositoryImpl(goalsDao)
        val trackingRepo : TrackingRepository = TrackingRepositoryImpl(trackingDao)
        val statisticsRepo : StatisticsRepository= StatisticsRepositoryImpl(goalsDao, trackingDao)

        //Services
        val goalService = GoalService(goalRepo, timeProvider)
        val rewardService = RewardService()
        val trackingService = TrackingServiceImpl(trackingRepo, userRepo, goalRepo, goalService, rewardService, timeProvider)
        val shopService = ShopService(userRepo)
        val profileService = ProfileService(this, userRepo, timeProvider, null)
        val statisticsService = StatisticsService(statisticsRepo)
        val trackingCleanupService = TrackingCleanupService(goalRepo,trackingRepo, timeProvider)

        // shared preferences (for storing last selected goal from GoalBottomDrawer)
        val focusPrefs = getSharedPreferences(DeepFocusConfig.PREFS_NAME, MODE_PRIVATE)
        val focusBlocker = SharedPrefsFocusBlocker(focusPrefs)
        val cleanupPrefs = SharedPrefCleanupPreferences(getSharedPreferences("app_prefs", MODE_PRIVATE))

        val supabase = SupabaseClientProvider.create(this)

        val supabaseAuthRemoteDataSource =
            SupabaseAuthRemoteDataSource(supabase)

        val supabaseProfileRemoteDataSource =
            SupabaseProfileRemoteDataSource(supabase)

        val supabaseCatRemoteDataSource =
            SupabaseCatRemoteDataSource(supabase)

        val supabaseFriendshipRemoteDataSource =
            SupabaseFriendshipRemoteDataSource(supabase)

        val supabaseGoalTrackingRemoteDataSource =
            SupabaseGoalTrackingRemoteDataSource(supabase)

        val supabaseAuthRepository: AuthRepository =
            AuthRepositoryImpl(remoteDataSource = supabaseAuthRemoteDataSource)

        val supabaseProfileRepository: ProfileRepository =
            ProfileRepositoryImpl(remoteDataSource = supabaseProfileRemoteDataSource)

        val supabaseCatRepository: CatCollectionRepository =
            CatCollectionRepositoryImpl(catRemoteDataSource = supabaseCatRemoteDataSource)

        val supabaseFriendshipRepository: FriendshipRepository =
            FriendshipRepositoryImpl(friendshipRemoteDataSource = supabaseFriendshipRemoteDataSource)

        val supabaseGoalTrackingRepository: GoalTrackingRepository =
            GoalTrackingRepositoryImpl(remoteDataSource = supabaseGoalTrackingRemoteDataSource)

        val supabaseSyncSnapshotRepository: SyncSnapshotRepository =
            SyncSnapshotRepositoryImpl(
                profileRepository = supabaseProfileRepository,
                catRepository = supabaseCatRepository,
                goalTrackingRepository = supabaseGoalTrackingRepository
            )


        val supabaseSyncService = SupabaseSyncService(
            userRepository = userRepo,
            goalRepository = goalRepo,
            trackingRepository = trackingRepo,
            authRepository = supabaseAuthRepository,
            profileRepository = supabaseProfileRepository,
            catRepository = supabaseCatRepository,
            friendshipRepository = supabaseFriendshipRepository,
            syncSnapshotRepository = supabaseSyncSnapshotRepository
        )

        // create ViewModel instances for this activity
        userViewModel = UserViewModel(shopService, supabaseSyncService, profileService)
        goalViewModel = GoalViewModel(goalService,focusPrefs, supabaseSyncService)
        // Factory for TrackingViewModel to preserve states across configuration changes
        trackingViewModel = ViewModelProvider(
            this,
            TrackingViewModelFactory(
                trackingService = trackingService,
                rewardService = rewardService,
                timeProvider = timeProvider,
                focusBlocker = focusBlocker,
                supabaseSyncService= supabaseSyncService
            )
        )[TrackingViewModel::class.java]
        // Use factory for StatisticsViewModel to preserve week offset across configuration changes
        statisticsViewModel = ViewModelProvider(
            this,
            StatisticsViewModelFactory(statisticsService)
        )[StatisticsViewModel::class.java]

        friendViewModel = ViewModelProvider(
            this,
            FriendViewModelFactory(supabaseSyncService)
        )[FriendViewModel::class.java]

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
//            if (supabaseSyncService.isSignedIn()) {
//                supabaseSyncService.checkAndSyncIfNeeded()
//            }
        }

        setContent {
            PurrsistenceTheme {
                // pass created ViewModels to MainScreen (scaffold)
                MainScreen(
                    userViewModel = userViewModel,
                    goalViewModel = goalViewModel,
                    trackingViewModel = trackingViewModel,
                    statisticsViewModel = statisticsViewModel,
                    friendViewModel = friendViewModel
                )
            }
        }
    }
}