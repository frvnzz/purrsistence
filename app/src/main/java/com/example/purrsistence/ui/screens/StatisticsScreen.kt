package com.example.purrsistence.ui.screens

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.GoalStatsList
import com.example.purrsistence.ui.components.WeekSelector
import com.example.purrsistence.ui.components.WeeklyChart
import com.example.purrsistence.ui.state.StatisticsUiState
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatMinutes
import com.example.purrsistence.ui.util.getWeekDisplay
import com.example.purrsistence.ui.util.safeAnnounce
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    setTopBar: (TopBarState) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val accessibilityManager = remember {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    LaunchedEffect(state.weekOffset) {
        val (label, dateRange) = getWeekDisplay(state.weekOffset)
        accessibilityManager.safeAnnounce("Viewing statistics for $label: $dateRange")
    }

    LaunchedEffect(Unit) {
        setTopBar(TopBarState(title = "Statistics"))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Statistics Screen" }
            .pointerInput(state.weekOffset) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    var xOffset = 0f
                    while (true) {
                        val change = awaitPointerEvent(PointerEventPass.Initial).changes.find { it.id == down.id } ?: break
                        xOffset += change.position.x - change.previousPosition.x
                        
                        //intercept horizontal drag to prevent child interaction, like chart scrolling
                        if (kotlin.math.abs(xOffset) > viewConfiguration.touchSlop) {
                            change.consume()
                        }
                        
                        if (!change.pressed) {
                            val threshold = 50.dp.toPx()
                            if (xOffset > threshold) {
                                viewModel.previousWeek()
                            } else if (xOffset < -threshold && state.weekOffset < 0) {
                                viewModel.nextWeek()
                            }
                            break
                        }
                    }
                }
            }
    ) {
        val isLandscape = this.maxWidth > 600.dp
        

        AnimatedContent(
            targetState = state.weekOffset,
            transitionSpec = {
                if (targetState < initialState) {
                    // sliding to the past: Content comes from left, exits to right
                    (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> width } + fadeOut())
                } else {
                    // sliding ahead: Content comes from right, exits to left
                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width } + fadeOut())
                }
            },
            label = "WeekTransition"
        ) { animatedWeekOffset ->
            androidx.compose.runtime.key(animatedWeekOffset) {
                if (isLandscape) {
                    LandscapeStatistics(viewModel, state)
                } else {
                    PortraitStatistics(viewModel, state)
                }
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
    val totalMinutes = remember(state.dailyStats) {
        state.dailyStats.sumOf { it.totalMinutes }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        WeekSelector(viewModel, state)
        
        TotalSummaryCard(totalMinutes)
        
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
    val totalMinutes = remember(state.dailyStats) {
        state.dailyStats.sumOf { it.totalMinutes }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        //left Column: Chart
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            WeeklyChart(state.dailyStats)
        }

        //right Column: Controls, Summary, List
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            WeekSelector(viewModel, state, compact = true)

            Spacer(modifier = Modifier.height(Spacing.md))

            //scrollable info section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                TotalSummaryCard(totalMinutes)
                GoalStatsList(state.goalStats)
            }
        }
    }
}

@Composable
private fun TotalSummaryCard(totalMinutes: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WEEKLY TOTAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatMinutes(totalMinutes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
