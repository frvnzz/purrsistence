package com.example.purrsistence.ui.viewmodel

import com.example.purrsistence.controller.TrackingNotificationController
import com.example.purrsistence.domain.controller.FakeTrackingNotificationController
import com.example.purrsistence.domain.focus.FakeFocusBlocker
import com.example.purrsistence.domain.model.types.SyncStatus
import com.example.purrsistence.domain.notifications.FakeSessionReminderScheduler
import com.example.purrsistence.domain.service.fakes.FakeSupabaseSyncService
import com.example.purrsistence.domain.service.fakes.FakeTrackingService
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.notifications.SessionReminderScheduler
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.ui.navigation.TrackingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private lateinit var trackingService: FakeTrackingService
    private lateinit var rewardService: RewardService
    private lateinit var blocker: FakeFocusBlocker
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var syncService: FakeSupabaseSyncService
    private lateinit var viewModel: TrackingViewModel
    private lateinit var notificationController: TrackingNotificationController
    private lateinit var reminderScheduler: SessionReminderScheduler

    private fun setup(testScheduler: TestCoroutineScheduler) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        trackingService = FakeTrackingService()
        rewardService = RewardService()
        blocker = FakeFocusBlocker()
        timeProvider = FakeTimeProvider(Instant.ofEpochMilli(0L))
        syncService = FakeSupabaseSyncService()
        notificationController = FakeTrackingNotificationController()
        reminderScheduler = FakeSessionReminderScheduler()

        viewModel = TrackingViewModel(
            trackingService = trackingService,
            rewardService = rewardService,
            timeProvider = timeProvider,
            focusBlocker = blocker,
            supabaseSyncService = syncService,
            trackingNotificationController = notificationController,
            sessionReminderScheduler = reminderScheduler
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stopTracking_showsWarning_whenDurationIsLessThanOneMinute() = runTest {
        setup(testScheduler)

        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        timeProvider.currentTime = Instant.ofEpochMilli(30_000L)
        advanceTimeBy(1001)
        runCurrent()

        viewModel.stopTracking()
        runCurrent()

        assertTrue(viewModel.uiState.value.showStopWarning)
        assertEquals(0, trackingService.stopCalls)

        viewModel.confirmStopTracking()
        runCurrent()
    }

    @Test
    fun stopTracking_stopsImmediately_whenDurationIsAtLeastOneMinute() = runTest {
        setup(testScheduler)

        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        timeProvider.currentTime = Instant.ofEpochMilli(61_000L)
        advanceTimeBy(1001)
        runCurrent()

        viewModel.stopTracking()
        runCurrent()

        assertFalse(viewModel.uiState.value.showStopWarning)
        assertEquals(1, trackingService.stopCalls)
    }

    @Test
    fun confirmStopTracking_updatesUiState_stopsNotification_schedulesReminder_syncs_andEmitsRewardsNavigation() = runTest {
        setup(testScheduler)

        val trackingEventDeferred = async { viewModel.events.first() }

        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        // Consume the first event emitted by startTrack()
        assertEquals(TrackingEvent.NavigateToTrackingScreen, trackingEventDeferred.await())

        // Now wait for the next event, which should come from confirmStopTracking()
        val rewardsEventDeferred = async { viewModel.events.first() }

        timeProvider.currentTime = Instant.ofEpochMilli(61_000L)
        advanceTimeBy(1001)
        runCurrent()

        viewModel.confirmStopTracking()
        runCurrent()

        val fakeNotificationController =
            notificationController as FakeTrackingNotificationController
        val fakeReminderScheduler =
            reminderScheduler as FakeSessionReminderScheduler
        val fakeSyncService =
            syncService as FakeSupabaseSyncService

        assertEquals(1, trackingService.stopCalls)
        assertFalse(viewModel.uiState.value.isTracking)
        assertFalse(viewModel.uiState.value.showStopWarning)

        assertEquals(1, blocker.stopCalls)
        assertEquals(1, fakeNotificationController.stopCalls)

        assertEquals(1, fakeReminderScheduler.scheduleCalls)
        assertEquals(1200L, fakeReminderScheduler.lastDelayMinutes)
        assertEquals(
            "The cats are pretending not to worry",
            fakeReminderScheduler.lastTitle
        )
        assertEquals(
            "The cats have checked the doorway twice and are trying to stay brave.",
            fakeReminderScheduler.lastMessage
        )

        assertEquals(SyncStatus.NOT_LINKED, fakeSyncService.syncAfterLocalTrackingSessionChanged())
        assertEquals(TrackingEvent.NavigateToRewardsScreen, rewardsEventDeferred.await())
    }

    @Test
    fun confirmStopTracking_stopsRegardlessOfTime() = runTest {
        setup(testScheduler)

        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        timeProvider.currentTime = Instant.ofEpochMilli(30_000L)
        advanceTimeBy(1001)
        runCurrent()

        viewModel.confirmStopTracking()
        runCurrent()

        assertFalse(viewModel.uiState.value.showStopWarning)
        assertEquals(1, trackingService.stopCalls)
    }

    @Test
    fun dismissStopWarning_hidesDialog() = runTest {
        setup(testScheduler)

        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        timeProvider.currentTime = Instant.ofEpochMilli(30_000L)
        advanceTimeBy(1001)
        runCurrent()

        viewModel.stopTracking()
        runCurrent()
        assertTrue(viewModel.uiState.value.showStopWarning)

        viewModel.dismissStopWarning()
        runCurrent()

        assertFalse(viewModel.uiState.value.showStopWarning)

        viewModel.confirmStopTracking()
        runCurrent()
    }

    @Test
    fun returnHome_emitsNavigateBackHome() = runTest {
        setup(testScheduler)

        val eventDeferred = async { viewModel.events.first() }

        viewModel.returnHome()
        runCurrent()

        assertEquals(TrackingEvent.NavigateBackHome, eventDeferred.await())
    }
}