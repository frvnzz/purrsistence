package com.example.purrsistence

import android.content.Context
import android.content.SharedPreferences
import com.example.purrsistence.data.focus.SharedPrefsFocusBlocker
import com.example.purrsistence.data.local.AppDatabase
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
import com.example.purrsistence.domain.time.TimeProvider
import com.example.purrsistence.focus.DeepFocusConfig
import com.example.purrsistence.service.CleanupScheduler
import com.example.purrsistence.service.GoalService
import com.example.purrsistence.service.ProfileService
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.ShopService
import com.example.purrsistence.service.StatisticsService
import com.example.purrsistence.service.SupabaseSyncService
import com.example.purrsistence.service.TrackingCleanupService
import com.example.purrsistence.service.TrackingService
import com.example.purrsistence.service.TrackingServiceImpl
import com.example.purrsistence.controller.TrackingNotificationController
import com.example.purrsistence.controller.TrackingNotificationControllerImpl
import com.example.purrsistence.notifications.SessionReminderScheduler
import com.example.purrsistence.notifications.SessionReminderSchedulerImpl

class AppContainer(
    private val context: Context,
) {

    // Database
    private val db: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val goalsDao by lazy { db.goalsDao() }
    val trackingDao by lazy { db.trackingDao() }
    val userDao by lazy { db.userDao() }

    // Core utilities
    val timeProvider: TimeProvider by lazy { SystemTimeProvider() }

    val focusPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(DeepFocusConfig.PREFS_NAME, Context.MODE_PRIVATE)
    }

    val focusBlocker by lazy {
        SharedPrefsFocusBlocker(focusPrefs)
    }

    val cleanupPrefs by lazy {
        SharedPrefCleanupPreferences(
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        )
    }

    // Local repositories
    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userDao, timeProvider)
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepositoryImpl(goalsDao)
    }

    val trackingRepository: TrackingRepository by lazy {
        TrackingRepositoryImpl(trackingDao)
    }

    val statisticsRepository: StatisticsRepository by lazy {
        StatisticsRepositoryImpl(goalsDao, trackingDao)
    }

    // Services
    val goalService by lazy {
        GoalService(goalRepository, timeProvider)
    }

    val rewardService by lazy {
        RewardService()
    }

    val trackingService: TrackingService by lazy {
        TrackingServiceImpl(
            trackingRepository,
            userRepository,
            goalRepository,
            goalService,
            rewardService,
            timeProvider
        )
    }

    val shopService by lazy {
        ShopService(userRepository, supabaseSyncService)
    }

    val profileService by lazy {
        ProfileService(context, userRepository, timeProvider, null)
    }

    val statisticsService by lazy {
        StatisticsService(statisticsRepository)
    }

    val trackingCleanupService by lazy {
        TrackingCleanupService(goalRepository, trackingRepository, timeProvider)
    }

    val cleanupScheduler by lazy {
        CleanupScheduler(cleanupPrefs, timeProvider, trackingCleanupService)
    }

    // Notifications
    val trackingNotificationController: TrackingNotificationController by lazy {
        TrackingNotificationControllerImpl(context)
    }

    val sessionReminderScheduler: SessionReminderScheduler by lazy {
        SessionReminderSchedulerImpl(context)
    }

    // Supabase
    val supabase by lazy {
        SupabaseClientProvider.create(context)
    }

    val supabaseAuthRemoteDataSource by lazy {
        SupabaseAuthRemoteDataSource(supabase)
    }

    val supabaseProfileRemoteDataSource by lazy {
        SupabaseProfileRemoteDataSource(supabase)
    }

    val supabaseCatRemoteDataSource by lazy {
        SupabaseCatRemoteDataSource(supabase)
    }

    val supabaseFriendshipRemoteDataSource by lazy {
        SupabaseFriendshipRemoteDataSource(supabase)
    }

    val supabaseGoalTrackingRemoteDataSource by lazy {
        SupabaseGoalTrackingRemoteDataSource(supabase)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(supabaseAuthRemoteDataSource)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(supabaseProfileRemoteDataSource)
    }

    val catCollectionRepository: CatCollectionRepository by lazy {
        CatCollectionRepositoryImpl(supabaseCatRemoteDataSource)
    }

    val friendshipRepository: FriendshipRepository by lazy {
        FriendshipRepositoryImpl(supabaseFriendshipRemoteDataSource)
    }

    val goalTrackingRepository: GoalTrackingRepository by lazy {
        GoalTrackingRepositoryImpl(supabaseGoalTrackingRemoteDataSource)
    }

    val syncSnapshotRepository: SyncSnapshotRepository by lazy {
        SyncSnapshotRepositoryImpl(
            profileRepository = profileRepository,
            catRepository = catCollectionRepository,
            goalTrackingRepository = goalTrackingRepository
        )
    }

    val supabaseSyncService by lazy {
        SupabaseSyncService(
            userRepository = userRepository,
            goalRepository = goalRepository,
            trackingRepository = trackingRepository,
            authRepository = authRepository,
            profileRepository = profileRepository,
            catRepository = catCollectionRepository,
            friendshipRepository = friendshipRepository,
            syncSnapshotRepository = syncSnapshotRepository,
            goalTrackingRepository = goalTrackingRepository
        )
    }
}