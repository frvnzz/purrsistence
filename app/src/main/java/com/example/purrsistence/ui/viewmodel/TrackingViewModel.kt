package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.domain.time.TimeProvider
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.TrackingService
import com.example.purrsistence.ui.navigation.TrackingEvent
import com.example.purrsistence.ui.state.TrackingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class TrackingViewModel(
    private val trackingService: TrackingService,
    private val rewardService: RewardService,
    private val timeProvider: TimeProvider,
    private val focusBlocker: FocusBlocker
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    private val _events = MutableSharedFlow<TrackingEvent>()
    val events: SharedFlow<TrackingEvent> = _events

    private var timerJob: Job? = null
    private var isDeepFocusSession = false

    private var pauseJob: Job? = null //for auto stop

    fun startTrack(
        goalId: Int,
        goalTitle: String,
        userId: Int,
        deepFocus: Boolean
    ) {
        viewModelScope.launch{
            val session = trackingService.startTracking(
                goalId = goalId,
                userId = userId,
                pauseReminder = false,
                deepFocus = deepFocus
            )

            isDeepFocusSession = deepFocus
            if (isDeepFocusSession) {
                focusBlocker.startBlocking()
            }

            _uiState.value = TrackingUiState(
                trackingId = session.id,
                goalId = session.goalId,
                goalTitle = goalTitle,
                startTime = session.startTime,
                elapsedMillis = 0L,
                isTracking = true
            )

            startTicker(session.startTime)
            _events.emit(TrackingEvent.NavigateToTrackingScreen)
        }
    }

    fun stopTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            val trackingId = state.trackingId ?: return@launch

            val stopResult = trackingService.stopTracking(trackingId) ?: return@launch

            timerJob?.cancel()
            timerJob = null

            pauseJob?.cancel()  //cancel auto-stop timer when stopping manually
            pauseJob = null

            if (isDeepFocusSession) {
                focusBlocker.stopBlocking()
                isDeepFocusSession = false
            }

            _uiState.value = state.copy(
                isTracking = false,
                rewardedCurrency = stopResult.rewardedCurrency,
                multiplier = stopResult.multiplier,
                sessionDurationMillis = stopResult.sessionDurationMillis,
                elapsedMillis = stopResult.sessionDurationMillis,
                goalCompletionReward = stopResult.goalCompletionReward,  //show goal completion reward in UI if applicable
                pauseAutoStopWarning = null
            )
        }
    }

    // return to HomeScreen after receiving a reward
    fun returnHome() {
        viewModelScope.launch {
            _events.emit(TrackingEvent.NavigateBackHome)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        pauseJob?.cancel()  //cancel auto-stop timer on ViewModel clear
        if (isDeepFocusSession) {
            focusBlocker.stopBlocking()
            isDeepFocusSession = false
        }
        super.onCleared()
    }

    // Realtime session updater (elapsed time)
    private fun startTicker(startTime: Instant) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                val state = _uiState.value

                // Don't update timer if paused
                if (!state.isPaused) {
                    val now = timeProvider.now()
                    val totalWallClockTime = Duration.between(startTime, now).toMillis()
                    val effectiveElapsed = (totalWallClockTime - state.totalPausedMillis).coerceAtLeast(0L)

                    // get the tracked minutes for multiplier calculation
                    val trackedMinutes = Duration.ofMillis(effectiveElapsed).toMinutes().toInt()
                    // live multiplier update for FocusTimerProgress component on TrackingScreen
                    val liveMultiplier =
                        rewardService.calculateRewardMultiplier(trackedMinutes)
                    // live progress to next multiplier (x2.0 is max multiplier)
                    val progressToNextMultiplier =
                        if (liveMultiplier >= 2.0) {
                            1f
                        } else {
                            (trackedMinutes % 15) / 15f
                        }

                    // update TrackingUIState
                    _uiState.value = state.copy(
                        elapsedMillis = effectiveElapsed,
                        liveMultiplier = liveMultiplier,
                        multiplierProgress = progressToNextMultiplier
                    )
                }

                delay(1000)
            }
        }
    }

    fun pauseTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            val trackingId = state.trackingId ?: return@launch

            println("pauseTracking: Starting pause for trackingId=$trackingId")

            if (trackingService.pauseTracking(trackingId)) {
                val now = timeProvider.now()
                val newState = state.copy(
                    isPaused = true,
                    currentPauseStart = now
                )
                _uiState.value = newState

                println("pauseTracking: Successfully paused. isPaused=${newState.isPaused}, currentPauseStart=$now, elapsedMillis=${newState.elapsedMillis}")

                startPauseTimer()  //Auto-stop after 1 hour
            } else {
                println("pauseTracking: Failed - service returned false")
            }
        }
    }

    fun resumeTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            val trackingId = state.trackingId ?: return@launch

            val pauseStart = state.currentPauseStart ?: return@launch

            if (trackingService.resumeTracking(trackingId)) {
                val now = timeProvider.now()

                val pausedAddition =
                    Duration.between(pauseStart, now).toMillis()

                val newTotalPaused =
                    state.totalPausedMillis + pausedAddition

                val resetWarning =
                    if (newTotalPaused > 15 * 60 * 1000)
                        "Multiplier reset due to long pause"
                    else null

                _uiState.value = state.copy(
                    isPaused = false,
                    totalPausedMillis = newTotalPaused,
                    currentPauseStart = null,
                    multiplierResetWarning = resetWarning,
                    pauseAutoStopWarning = null
                )

                pauseJob?.cancel()
            }
        }
    }

    private fun startPauseTimer() { //Auto-stop after 1 hour of being paused
        pauseJob?.cancel()
        pauseJob = viewModelScope.launch {
            /*delay(14 * 60 * 1000) // 14 minutes till toast reminder that multiplier will be reset
            _uiState.value = _uiState.value.copy(
                multiplierResetWarning = "Reward multiplier will reset in 1 minute due to pause."
            )

            delay(1 * 60 * 1000) // 1 minute (Total 15 min) after that time multiplier resets
            _uiState.value = _uiState.value.copy(
                multiplierResetWarning = "Multiplier reset due to long pause"
            )*/

            delay(55 * 60 * 1000) // 55 min  tracking will stop in 5 min reminder
            _uiState.value = _uiState.value.copy(
                pauseAutoStopWarning = "Tracking will stop in 5 minutes due to prolonged pause."
            )

            delay(5 * 60 * 1000) // 5 minutes (Total 60 min)
            stopTracking()  // Auto-stop
        }
    }
}