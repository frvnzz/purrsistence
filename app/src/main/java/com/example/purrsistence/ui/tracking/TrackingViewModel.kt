package com.example.purrsistence.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackingViewModel(
    private val repository: TrackingRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    private val _events = MutableSharedFlow<TrackingEvent>()
    val events: SharedFlow<TrackingEvent> = _events

    private var timerJob: Job? = null

    fun startTrack(goalId: Int, userId: Int){
        viewModelScope.launch{
            val session = repository.startTracking(
                goalId = goalId,
                userId = userId,
                pauseReminder = false
            )

            _uiState.value = TrackingUiState(
                trackingId = session.trackingId,
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
        viewModelScope.launch{
            val trackingId = _uiState.value.trackingId ?: return@launch
            repository.stopTracking(trackingId)

            timerJob?.cancel()
            timerJob = null

            _uiState.value = TrackingUiState()
            _events.emit(TrackingEvent.NavigateBackHome)
        }
    }

    private fun startTicker(startTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                _uiState.value = _uiState.value.copy(
                    elapsedMillis = timeProvider.now() - startTime
                )
                delay(1000)
            }
        }
    }

}