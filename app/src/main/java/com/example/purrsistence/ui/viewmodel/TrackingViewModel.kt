package com.example.purrsistence.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.controller.TrackingNotificationController
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.domain.time.TimeProvider
import com.example.purrsistence.notifications.SessionReminderScheduler
import com.example.purrsistence.notifications.SessionReminderWorker
import com.example.purrsistence.service.CleanupScheduler
import com.example.purrsistence.service.SupabaseSyncService
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.TrackingForegroundService
import com.example.purrsistence.service.TrackingService
import com.example.purrsistence.service.TrackingSyncService
import com.example.purrsistence.ui.navigation.TrackingEvent
import com.example.purrsistence.ui.state.TrackingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class TrackingViewModel(
    private val trackingService: TrackingService,
    private val rewardService: RewardService,
    private val timeProvider: TimeProvider,
    private val focusBlocker: FocusBlocker,
    private val supabaseSyncService: TrackingSyncService,
    private val trackingNotificationController: TrackingNotificationController,
    private val sessionReminderScheduler: SessionReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    private val _events = MutableSharedFlow<TrackingEvent>()
    val events: SharedFlow<TrackingEvent> = _events

    private var timerJob: Job? = null
    private var pauseJob: Job? = null //for auto stop

    init {
        restoreTrackingSession()
    }

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

            if (session.deepFocus) {
                focusBlocker.startBlocking()
            }

            _uiState.update {
                TrackingUiState(
                    trackingId = session.id,
                    goalId = session.goalId,
                    goalTitle = goalTitle,
                    startTime = session.startTime,
                    elapsedMillis = 0L,
                    isTracking = true
                )
            }

            trackingNotificationController.startTrackingNotification(
                trackingId = session.id,
                goalTitle = goalTitle,
                startTimeMillis = session.startTime.toEpochMilli()
            )

            sessionReminderScheduler.cancelReminder()

            startTicker(session.startTime)
            _events.emit(TrackingEvent.NavigateToTrackingScreen)
        }
    }

    fun stopTracking() {
        val state = _uiState.value
        //if tracked less than a minute, show warning because no reward would be given
        if (state.elapsedMillis < 60_000L) {
            _uiState.update { it.copy(showStopWarning = true) }
        } else {
            confirmStopTracking()
        }
    }

    fun dismissStopWarning() {
        _uiState.update { it.copy(showStopWarning = false) }
    }

    fun confirmStopTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            val trackingId = state.trackingId ?: return@launch

            val stopResult = trackingService.stopTracking(trackingId) ?: return@launch

            timerJob?.cancel()
            timerJob = null

            pauseJob?.cancel()  //cancel auto-stop timer when stopping manually
            pauseJob = null

            focusBlocker.stopBlocking()

            _uiState.update {
                it.copy(
                    isTracking = false,
                    rewardedCurrency = stopResult.rewardedCurrency,
                    multiplier = stopResult.multiplier,
                    sessionDurationMillis = stopResult.sessionDurationMillis,
                    elapsedMillis = stopResult.sessionDurationMillis,
                    goalCompletionReward = stopResult.goalCompletionReward,  //show goal completion reward in UI if applicable
                    pauseAutoStopWarning = null,
                    showStopWarning = false
                )
            }

            sessionReminderScheduler.scheduleReminder(
                delayMinutes = 1200,
                title = "The cats are pretending not to worry",
                message = "The cats have checked the doorway twice and are trying to stay brave."
            )

            trackingNotificationController.stopTrackingNotification()

            _events.emit(TrackingEvent.NavigateToRewardsScreen)
            supabaseSyncService.syncAfterLocalTrackingSessionChanged()
        }
    }

    // return to HomeScreen after receiving a reward
    fun returnHome() {
        viewModelScope.launch {
            _events.emit(TrackingEvent.NavigateBackHome)
        }
    }

    private fun restoreTrackingSession() {
        viewModelScope.launch {

            val session = trackingService.getActiveTrackingSession() ?: return@launch

            val goalTitle = trackingService.getTrackingGoalTitle(session.goalId)

            val now = timeProvider.now()

            val effectiveElapsed = session.effectiveDuration(now).toMillis()

            val trackedMinutes = session.getEffectiveMinutesSinceLastReset(now)

            val liveMultiplier = rewardService.calculateRewardMultiplier(trackedMinutes)

            val progressToNextMultiplier =
                if (liveMultiplier >= 2.0) {
                    1f
                } else {
                    (trackedMinutes % 15) / 15f
                }

            val resetWarning = if (session.hasLongPause(now)) "Multiplier reset due to long pause" else null

            _uiState.update {
                it.copy(
                    trackingId = session.id,
                    goalId = session.goalId,
                    goalTitle = goalTitle,
                    startTime = session.startTime,
                    elapsedMillis = effectiveElapsed,
                    isTracking = true,
                    liveMultiplier = liveMultiplier,
                    multiplierProgress = progressToNextMultiplier,
                    isPaused = session.currentPauseStart != null,
                    totalPausedMillis = session.getTotalPausedMillis(now),
                    currentPauseStart = session.currentPauseStart,
                    multiplierResetWarning = resetWarning,
                    checkpointedCurrency = session.getCheckpointedCurrency(),
                    minutesSinceReset = trackedMinutes
                )
            }

            if (session.deepFocus) {
                focusBlocker.startBlocking()
            }

            startTicker(session.startTime)

            if (session.currentPauseStart != null) {
                startPauseTimer()
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        pauseJob?.cancel()  //cancel auto-stop timer on ViewModel clear
        focusBlocker.stopBlocking()
        super.onCleared()
    }

    // Realtime session updater (elapsed time)
    private fun startTicker(startTime: Instant) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                val session = trackingService.getActiveTrackingSession()

                if (session == null) {
                    break // Stop ticker if no session is active
                }

                val now = timeProvider.now()
                val totalElapsedDuration = Duration.between(startTime, now).toMillis()
                val totalPaused = session.getTotalPausedMillis(now)
                val effectiveElapsedTotal = (totalElapsedDuration - totalPaused).coerceAtLeast(0L)

                val trackedMinutesSinceReset = session.getEffectiveMinutesSinceLastReset(now)
                val liveMultiplier = rewardService.calculateRewardMultiplier(trackedMinutesSinceReset)

                val progressToNextMultiplier =
                    if (liveMultiplier >= 2.0) {
                        1f
                    } else {
                        (trackedMinutesSinceReset % 15) / 15f
                    }

                _uiState.update { state ->
                    val currentPauseDuration = if (state.isPaused) Duration.between(state.currentPauseStart!!, now).toMinutes() else 0L

                    val isResetByCurrentPause = currentPauseDuration >= 15 //if 15 min limit is reached
                    val finalMultiplier = if (isResetByCurrentPause) 1.0 else liveMultiplier //if reset occurred set to 1 otherwise set to current
                    val finalProgress = if (isResetByCurrentPause) 0f else progressToNextMultiplier //set progress to 0 if reset
                    val finalMinutes = if (isResetByCurrentPause) 0 else trackedMinutesSinceReset
                    val resetWarning = if (isResetByCurrentPause || state.multiplierResetWarning != null) "Multiplier reset due to long pause" else null

                    // update TrackingUIState
                    state.copy(
                        elapsedMillis = effectiveElapsedTotal,
                        liveMultiplier = finalMultiplier,
                        multiplierProgress = finalProgress,
                        multiplierResetWarning = resetWarning,
                        totalPausedMillis = totalPaused,
                        checkpointedCurrency = session.getCheckpointedCurrency(),
                        minutesSinceReset = finalMinutes
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

            if (trackingService.pauseTracking(trackingId)) {
                val now = timeProvider.now()
                _uiState.update {
                    it.copy(
                        isPaused = true,
                        currentPauseStart = now
                    )
                }

                startPauseTimer()  //Auto-stop after 1 hour
            }
        }
    }

    fun resumeTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            val trackingId = state.trackingId ?: return@launch

            if (trackingService.resumeTracking(trackingId)) {
                val session = trackingService.getActiveTrackingSession()
                val now = timeProvider.now()

                val newTotalPaused = session?.getTotalPausedMillis(now) ?: state.totalPausedMillis
                val isReset = session?.hasLongPause(now) == true
                val resetWarning = if (isReset) "Multiplier reset due to long pause" else null

                //reset multiplier and progress if it was a long pause
                val newMultiplier = if (isReset) 1.0 else state.liveMultiplier
                val newProgress = if (isReset) 0f else state.multiplierProgress
                val newMinutes = if (isReset) 0 else state.minutesSinceReset

                _uiState.update {
                    it.copy(
                        isPaused = false,
                        totalPausedMillis = newTotalPaused,
                        currentPauseStart = null,
                        multiplierResetWarning = resetWarning,
                        pauseAutoStopWarning = null,
                        liveMultiplier = newMultiplier,
                        multiplierProgress = newProgress,
                        minutesSinceReset = newMinutes,
                        checkpointedCurrency = session?.getCheckpointedCurrency() ?: it.checkpointedCurrency
                    )
                }

                pauseJob?.cancel()
            }
        }
    }

    private fun startPauseTimer() { //Auto-stop after 1 hour of being paused
        pauseJob?.cancel()
        pauseJob = viewModelScope.launch {
            delay(55 * 60 * 1000) // 55 min  tracking will stop in 5 min reminder
            _uiState.update {
                it.copy(
                    pauseAutoStopWarning = "Tracking will stop in 5 minutes due to prolonged pause."
                )
            }

            delay(5 * 60 * 1000) // 5 minutes (Total 60 min)
            confirmStopTracking()  // Auto-stop
        }
    }

    fun refreshTrackingState() {
        viewModelScope.launch {
            val activeSession = trackingService.getActiveTrackingSession()

            if (activeSession == null) {
                timerJob?.cancel()
                timerJob = null

                _uiState.value = TrackingUiState()

                _events.emit(TrackingEvent.NavigateBackHome)
                return@launch
            }

            _uiState.value = TrackingUiState(
                trackingId = activeSession.id,
                goalId = activeSession.goalId,
                startTime = activeSession.startTime,
                elapsedMillis = timeProvider.now().toEpochMilli() - activeSession.startTime.toEpochMilli(),
                isTracking = true
            )

            startTicker(activeSession.startTime)
        }
    }
}