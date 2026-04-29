package com.example.purrsistence.ui.tracking


import com.example.purrsistence.domain.focus.FakeFocusBlocker
import com.example.purrsistence.domain.service.fakes.FakeTrackingService
import com.example.purrsistence.domain.time.FakeTimeProvider
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
        val blocker = FakeFocusBlocker()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))

        val viewModel = TrackingViewModel(
            trackingService = trackingService,
            timeProvider = timeProvider,
            focusBlocker = blocker
        )

        viewModel.startTrack(goalId = 9, userId = 1, deepFocus = true)
        runCurrent()

        assertEquals(1, blocker.startCalls)
        assertEquals(9, trackingService.lastStartedGoalId)
        assertEquals(1, trackingService.lastStartedUserId)
        assertEquals(1, trackingService.startCalls)

        viewModel.stopTracking()
        runCurrent()
    }

    @Test
    fun startTrack_withoutDeepFocus_doesNotStartBlocking() = runTest {
        val trackingService = FakeTrackingService()
        val blocker = FakeFocusBlocker()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))

        val viewModel = TrackingViewModel(
            trackingService = trackingService,
            timeProvider = timeProvider,
            focusBlocker = blocker
        )

        viewModel.startTrack(goalId = 9, userId = 1, deepFocus = false)
        runCurrent()

        assertEquals(0, blocker.startCalls)
        assertEquals(9, trackingService.lastStartedGoalId)
        assertEquals(1, trackingService.lastStartedUserId)
        assertEquals(1, trackingService.startCalls)

        viewModel.stopTracking()
        runCurrent()
    }

    @Test
    fun stopTracking_afterDeepFocusSession_stopsBlocking() = runTest {
        val trackingService = FakeTrackingService()
        val blocker = FakeFocusBlocker()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1_000L))

        val viewModel = TrackingViewModel(
            trackingService = trackingService,
            timeProvider = timeProvider,
            focusBlocker = blocker
        )

        viewModel.startTrack(goalId = 9, userId = 1, deepFocus = true)
        runCurrent()

        viewModel.stopTracking()
        runCurrent()

        assertEquals(1, blocker.stopCalls)
        assertEquals(listOf(1), trackingService.stoppedTrackingIds)
        assertEquals(1, trackingService.stopCalls)
    }
}