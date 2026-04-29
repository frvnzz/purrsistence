package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.domain.time.TimeProvider
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
    private val timeProvider: TimeProvider,
    private val focusBlocker: FocusBlocker
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    private val _events = MutableSharedFlow<TrackingEvent>()
    val events: SharedFlow<TrackingEvent> = _events

    private var timerJob: Job? = null
    private var isDeepFocusSession = false

    fun startTrack(goalId: Int, userId: Int, deepFocus: Boolean) {
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

            if (isDeepFocusSession) {
                focusBlocker.stopBlocking()
                isDeepFocusSession = false
            }

            _uiState.value = state.copy(
                isTracking = false,
                rewardedCurrency = stopResult.rewardedCurrency,
                multiplier = stopResult.multiplier,
                sessionDurationMillis = stopResult.sessionDurationMillis,
                elapsedMillis = stopResult.sessionDurationMillis
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
        if (isDeepFocusSession) {
            focusBlocker.stopBlocking()
            isDeepFocusSession = false
        }
        super.onCleared()
    }

    private fun startTicker(startTime: Instant) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                val elapsed = Duration.between(startTime, timeProvider.now()).toMillis()

                _uiState.value = _uiState.value.copy(
                    elapsedMillis = elapsed.coerceAtLeast(0L)
                )

                delay(1000)
            }
        }
    }
}