package com.example.purrsistence.ui.tracking


import com.example.purrsistence.domain.controller.FakeTrackingNotificationController
import com.example.purrsistence.domain.focus.FakeFocusBlocker
import com.example.purrsistence.domain.notifications.FakeSessionReminderScheduler
import com.example.purrsistence.domain.service.fakes.FakeSupabaseSyncService
import com.example.purrsistence.domain.service.fakes.FakeTrackingService
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant


@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelDeepFocusTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startTrack_withDeepFocus_startsBlocking() = runTest {
        val trackingService = FakeTrackingService()
        val rewardService = RewardService()
        val blocker = FakeFocusBlocker()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val trackingSyncService = FakeSupabaseSyncService()
        val notificationController = FakeTrackingNotificationController()
        val reminderScheduler = FakeSessionReminderScheduler()

        val viewModel = TrackingViewModel(
            trackingService = trackingService,
            rewardService = rewardService,
            timeProvider = timeProvider,
            focusBlocker = blocker,
            supabaseSyncService = trackingSyncService,
            trackingNotificationController = notificationController,
            sessionReminderScheduler = reminderScheduler,
        )

        viewModel.startTrack(goalId = 9, goalTitle = "Test Goal", userId = 1, deepFocus = true)
        runCurrent()

        assertEquals(1, blocker.startCalls)
        assertEquals(9, trackingService.lastStartedGoalId)
        assertEquals(1, trackingService.lastStartedUserId)
        assertEquals(1, trackingService.startCalls)

        viewModel.confirmStopTracking()
        runCurrent()
    }

        @Test
        fun startTrack_withoutDeepFocus_doesNotStartBlocking() = runTest {
            val trackingService = FakeTrackingService()
            val rewardService = RewardService()
            val blocker = FakeFocusBlocker()
            val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
            val trackingSyncService = FakeSupabaseSyncService()
            val notificationController = FakeTrackingNotificationController()
            val reminderScheduler = FakeSessionReminderScheduler()

            val viewModel = TrackingViewModel(
                trackingService = trackingService,
                rewardService = rewardService,
                timeProvider = timeProvider,
                focusBlocker = blocker,
                supabaseSyncService = trackingSyncService,
                trackingNotificationController = notificationController,
                sessionReminderScheduler = reminderScheduler,
            )

        viewModel.startTrack(goalId = 9, goalTitle = "Test Goal", userId = 1, deepFocus = false)
        runCurrent()

        assertEquals(0, blocker.startCalls)
        assertEquals(9, trackingService.lastStartedGoalId)
        assertEquals(1, trackingService.lastStartedUserId)
        assertEquals(1, trackingService.startCalls)

        viewModel.confirmStopTracking()
        runCurrent()
    }

    @Test
    fun stopTracking_afterDeepFocusSession_stopsBlocking() = runTest {
        val trackingService = FakeTrackingService()
        val rewardService = RewardService()
        val blocker = FakeFocusBlocker()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))
        val trackingSyncService = FakeSupabaseSyncService()
        val notificationController = FakeTrackingNotificationController()
        val reminderScheduler = FakeSessionReminderScheduler()

        val viewModel = TrackingViewModel(
            trackingService = trackingService,
            rewardService = rewardService,
            timeProvider = timeProvider,
            focusBlocker = blocker,
            supabaseSyncService = trackingSyncService,
            trackingNotificationController = notificationController,
            sessionReminderScheduler = reminderScheduler,
        )

        viewModel.startTrack(goalId = 9, goalTitle = "Test Goal", userId = 1, deepFocus = true)
        runCurrent()

        viewModel.confirmStopTracking()
        runCurrent()

        assertEquals(1, blocker.stopCalls)
        assertEquals(listOf(1), trackingService.stoppedTrackingIds)
        assertEquals(1, trackingService.stopCalls)
    }
}