package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.GoalStatsList
import com.example.purrsistence.ui.components.WeekSelector
import com.example.purrsistence.ui.components.WeeklyChart
import com.example.purrsistence.ui.state.StatisticsUiState
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    setTopBar: (TopBarState) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        setTopBar(TopBarState(title = "Statistics"))
    }

    if (state.isLoading) {
        LoadingState()
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = this.maxWidth > 600.dp
        if (isLandscape) {
            LandscapeStatistics(viewModel, state)
        } else {
            PortraitStatistics(viewModel, state)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun PortraitStatistics(
    viewModel: StatisticsViewModel,
    state: StatisticsUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        WeekSelector(viewModel, state)
        Spacer(modifier = Modifier.height(Spacing.md))
        WeeklyChart(state.dailyStats)
        Spacer(modifier = Modifier.height(Spacing.xl))
        GoalStatsList(state.goalStats)
    }
}

@Composable
private fun LandscapeStatistics(
    viewModel: StatisticsViewModel,
    state: StatisticsUiState
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Fixed chart on the left
        Box(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        ) {
            WeeklyChart(state.dailyStats)
        }

        // Scrollable content on the right
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            WeekSelector(viewModel, state, compact = true)
            Spacer(modifier = Modifier.height(Spacing.md))
            GoalStatsList(state.goalStats)
        }
    }
}