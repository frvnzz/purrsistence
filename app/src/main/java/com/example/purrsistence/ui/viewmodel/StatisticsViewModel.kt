package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.service.StatisticsService
import com.example.purrsistence.ui.state.StatisticsUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val statisticsService: StatisticsService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState.asStateFlow()

    private var currentJob: Job? = null

    companion object {
        private const val WEEK_OFFSET_KEY = "week_offset"
    }

    init {
        // Restore weekOffset from saved state, default to 0 if not found
        val savedOffset = (savedStateHandle.get<Int>(WEEK_OFFSET_KEY) ?: 0).coerceAtMost(0)
        loadStats(savedOffset)
    }

    private fun loadStats(offset: Int) {
        val safeOffset = offset.coerceAtMost(0)
        currentJob?.cancel()

        _uiState.value = _uiState.value.copy(
            weekOffset = safeOffset,
            isLoading = true
        )

        // Save weekOffset for configuration changes (rotation)
        savedStateHandle[WEEK_OFFSET_KEY] = safeOffset

        currentJob = viewModelScope.launch {
            statisticsService.getWeeklyStats(safeOffset).collect { (daily, goals) ->
                _uiState.value = _uiState.value.copy(
                    weekOffset = safeOffset,
                    dailyStats = daily,
                    goalStats = goals,
                    isLoading = false
                )
            }
        }
    }

    fun previousWeek() {
        loadStats(_uiState.value.weekOffset - 1)
    }

    fun nextWeek() {
        loadStats(_uiState.value.weekOffset + 1)
    }
}