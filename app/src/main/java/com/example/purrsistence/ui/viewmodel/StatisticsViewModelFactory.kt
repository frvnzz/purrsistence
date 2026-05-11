package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.purrsistence.service.StatisticsService

class StatisticsViewModelFactory(
    private val statisticsService: StatisticsService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val savedStateHandle = extras.createSavedStateHandle()
        return StatisticsViewModel(statisticsService, savedStateHandle) as T
    }
}
