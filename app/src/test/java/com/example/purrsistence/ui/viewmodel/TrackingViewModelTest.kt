package com.example.purrsistence.ui.viewmodel

import com.example.purrsistence.domain.focus.FakeFocusBlocker
import com.example.purrsistence.domain.service.fakes.FakeSupabaseSyncService
import com.example.purrsistence.domain.service.fakes.FakeTrackingService
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.RewardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var trackingService: FakeTrackingService
    private lateinit var rewardService: RewardService
    private lateinit var blocker: FakeFocusBlocker
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var syncService: FakeSupabaseSyncService
    private lateinit var viewModel: TrackingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        trackingService = FakeTrackingService()
        rewardService = RewardService()
        blocker = FakeFocusBlocker()
        timeProvider = FakeTimeProvider(Instant.ofEpochMilli(0L))
        syncService = FakeSupabaseSyncService()

        viewModel = TrackingViewModel(
            trackingService = trackingService,
            rewardService = rewardService,
            timeProvider = timeProvider,
            focusBlocker = blocker,
            supabaseSyncService = syncService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stopTracking_showsWarning_whenDurationIsLessThanOneMinute() = runTest {
        // Start tracking session
        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        //set elapsed time
        timeProvider.currentTime = Instant.ofEpochMilli(30_000L)

        //force update UI state by advancing the virtual clock so the ticker runs
        advanceTimeBy(1001)
        runCurrent()

        //call stopTracking, should show warning
        viewModel.stopTracking()
        runCurrent()

        assertTrue("Warning should be shown", viewModel.uiState.value.showStopWarning)
        assertEquals("Service stop should not have been called", 0, trackingService.stopCalls)

        //cleanup: stop tracking to end the ticker loop
        viewModel.confirmStopTracking()
        runCurrent()
    }

    @Test
    fun stopTracking_stopsImmediately_whenDurationIsMoreThanOneMinute() = runTest {
        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        // 61000 - 1000 = 60000ms (1 minute)
        timeProvider.currentTime = Instant.ofEpochMilli(61_000L)
        advanceTimeBy(1001)
        runCurrent()

        viewModel.stopTracking()
        runCurrent()

        assertFalse("Warning should not be shown", viewModel.uiState.value.showStopWarning)
        assertEquals("Service stop should have been called", 1, trackingService.stopCalls)
    }

    @Test
    fun confirmStopTracking_stopsRegardlessOfTime() = runTest {
        viewModel.startTrack(1, "Goal", 1, false)
        runCurrent()

        timeProvider.currentTime = Instant.ofEpochMilli(30_000L)
        advanceTimeBy(1001)
        runCurrent()

        //confirmStopTracking bypasses the 1 minute check
        viewModel.confirmStopTracking()
        runCurrent()

        assertFalse("Warning should be false after stop", viewModel.uiState.value.showStopWarning)
        assertEquals("Service stop should have been called", 1, trackingService.stopCalls)
    }

    @Test
    fun dismissStopWarning_hidesDialog() = runTest {
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

        //cleanup: stop tracking to end the ticker loop
        viewModel.confirmStopTracking()
        runCurrent()
    }
}
