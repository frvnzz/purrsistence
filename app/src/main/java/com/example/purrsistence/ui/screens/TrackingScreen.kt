package com.example.purrsistence.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.tracking.FocusTimerProgress
import com.example.purrsistence.ui.components.tracking.TrackingActionButton
import com.example.purrsistence.ui.navigation.TrackingEvent
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatDuration
import com.example.purrsistence.ui.viewmodel.TrackingViewModel

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onNavigateBackHome: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.pauseAutoStopWarning) {
        state.pauseAutoStopWarning?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state.multiplierResetWarning) {
        state.multiplierResetWarning?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is TrackingEvent.NavigateBackHome) {
                onNavigateBackHome()
            }
        }
    }

    if (state.rewardedCurrency != null) {

        // REWARD CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl)
        ) {
            // Center content of the box
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = state.goalTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                Text(
                    text = "+${state.rewardedCurrency} coins",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                val effectiveSessionMillis =
                    (state.sessionDurationMillis ?: 0L) - state.totalPausedMillis

                Text(
                    text = formatDuration(effectiveSessionMillis.coerceAtLeast(0L)),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "Multiplier x${"%.2f".format(state.multiplier ?: 1.0)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                if ((state.goalCompletionReward ?: 0) > 0) {

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Text(
                        text = "GOAL COMPLETED",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "+${state.goalCompletionReward} bonus coins",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // Bottom Button (return home)
            TrackingActionButton(
                text = "Return Home",
                onClick = viewModel::returnHome,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.xl)
            )
        }

    } else {
        // MAIN TIME TRACKER CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg)
        ) {
            // Center content of the Box
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "TRACKING",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = state.goalTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                FocusTimerProgress(
                    elapsedMillis = state.elapsedMillis,
                    pausedMillis = state.totalPausedMillis,
                    multiplier = state.liveMultiplier.toFloat(),
                    multiplierProgress = state.multiplierProgress,
                    isPaused = state.isPaused
                )
            }
            // Bottom Buttons (Pause + Finish Session)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.xl),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxl)
            ) {

                TrackingActionButton(
                    text = if (state.isPaused) "Resume" else "Pause",
                    onClick = {
                        if (state.isPaused) {
                            viewModel.resumeTracking()
                        } else {
                            viewModel.pauseTracking()
                        }
                    }
                )

                TrackingActionButton(
                    text = "Finish",
                    onClick = viewModel::stopTracking
                )
            }
        }
    }
}