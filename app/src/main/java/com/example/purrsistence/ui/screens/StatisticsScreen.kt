package com.example.purrsistence.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
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
    var dragAmount by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        setTopBar(TopBarState(title = "Statistics"))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Statistics Screen" }
            .pointerInput(state.weekOffset) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmountValue ->
                        dragAmount += dragAmountValue
                    },
                    onDragEnd = {
                        if (dragAmount > 50) {
                            viewModel.previousWeek()
                        } else if (dragAmount < -50) {
                            if (state.weekOffset < 0) {
                                viewModel.nextWeek()
                            }
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = {
                        dragAmount = 0f
                    }
                )
            }
    ) {
        val isLandscape = this.maxWidth > 600.dp
        

        //sliding transition, wrap content in AnimatedContent
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                when {
                    targetState.weekOffset < initialState.weekOffset -> {
                        //sliding to the past: Content comes from left, exits to right
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                    targetState.weekOffset > initialState.weekOffset -> {
                        //sliding ahead: Content comes from right, exits to left
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    }
                    else -> {
                        //update within the same week -> no animation
                        fadeIn() togetherWith fadeOut()
                    }
                }.using(
                    SizeTransform(clip = false)
                )
            },
            label = "WeekTransition"
        ) { animatedState ->
            if (isLandscape) {
                LandscapeStatistics(viewModel, animatedState)
            } else {
                PortraitStatistics(viewModel, animatedState)
            }
        }
        
        //loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 100.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
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