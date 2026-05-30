package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.purrsistence.data.local.repository.FakeStatisticsRepository
import com.example.purrsistence.service.StatisticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `restores saved offset and clamps positive to zero`() = runTest {
        val repo = FakeStatisticsRepository(emptyList(), emptyList())
        val service = StatisticsService(repo)
        val saved = SavedStateHandle(mapOf("week_offset" to 2))

        val vm = StatisticsViewModel(service, saved)

        // advance coroutines to let ViewModel collect
        testDispatcher.scheduler.advanceUntilIdle()

        // initial UI state should be clamped to 0
        val state = vm.uiState.value
        assertEquals(0, state.weekOffset)
    }

    @Test
    fun `previous and next move between past weeks and do not go into future`() = runTest {
        val repo = FakeStatisticsRepository(emptyList(), emptyList())
        val service = StatisticsService(repo)
        val saved = SavedStateHandle()
        val vm = StatisticsViewModel(service, saved)

        testDispatcher.scheduler.advanceUntilIdle()
        // start at current week
        assertEquals(0, vm.uiState.value.weekOffset)

        vm.previousWeek()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(-1, vm.uiState.value.weekOffset)

        // next should go back to current (0)
        vm.nextWeek()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, vm.uiState.value.weekOffset)

        // calling next on current should keep it at 0 (cannot go future)
        vm.nextWeek()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, vm.uiState.value.weekOffset)
    }
}

