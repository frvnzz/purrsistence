package com.example.purrsistence.ui.screens

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.purrsistence.ui.components.tracking.FinishTrackingDialog
import com.example.purrsistence.ui.components.tracking.FocusTimerProgress
import com.example.purrsistence.ui.components.tracking.TrackingActionButton
import com.example.purrsistence.ui.components.tracking.TrackingStopWarningDialog
import com.example.purrsistence.ui.theme.DarkTertiary
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.safeAnnounce
import com.example.purrsistence.ui.viewmodel.TrackingViewModel

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val accessibilityManager = remember {
        context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }
    var accessibilityAnnouncement by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // handle back navigation (show dialog to finish session)
    BackHandler(enabled = state.isTracking) {
        viewModel.stopTracking()
    }

    LaunchedEffect(state.pauseAutoStopWarning) {
        state.pauseAutoStopWarning?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshTrackingState()
        }
    }

    LaunchedEffect(state.multiplierResetWarning) {
        state.multiplierResetWarning?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    if (state.showFinishDialog) {
        FinishTrackingDialog(
            onDismiss = viewModel::dismissFinishDialog,
            onConfirm = viewModel::confirmStopTracking
        )
    }

    if (state.showStopWarning) {
        TrackingStopWarningDialog(
            onDismiss = viewModel::dismissStopWarning,
            onConfirm = viewModel::confirmStopTracking
        )
    }

    LaunchedEffect(state.isPaused) {
        val message = if (state.isPaused) "Tracking paused" else "Tracking resumed"
        accessibilityManager.safeAnnounce(message)
    }

    // Invisible element to trigger accessibility announcements via LiveRegion
    Box(
        Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = accessibilityAnnouncement
        }
    )

    if (isLandscape) {
        // LANDSCAPE MODE
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg)
                .semantics { isTraversalGroup = true },
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT SECTION
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        isTraversalGroup = true
                        traversalIndex = 0f
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "TRACKING",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .focusable()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = state.goalTitle,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(Spacing.xl))

            // TIMER
            FocusTimerProgress(
                elapsedMillis = state.elapsedMillis,
                multiplier = state.liveMultiplier.toFloat(),
                multiplierProgress = state.multiplierProgress,
                checkpointedCurrency = state.checkpointedCurrency,
                minutesSinceReset = state.minutesSinceReset,
                isPaused = state.isPaused,
                modifier = Modifier
                    .weight(1.4f)
                    .semantics {
                        isTraversalGroup = true
                        traversalIndex = 2f
                    }
            )

            Spacer(modifier = Modifier.width(Spacing.xl))

            // BUTTONS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        isTraversalGroup = true
                        traversalIndex = 1f
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
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
                    containerColor = DarkTertiary,
                    onClick = viewModel::stopTracking
                )
            }
        }

    } else {

        // PORTRAIT MODE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg)
        ) {

            // CENTER CONTENT
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics { isTraversalGroup = true }
                ) {
                    Text(
                        text = "TRACKING",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .focusable()
                            .semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = state.goalTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))

                FocusTimerProgress(
                    elapsedMillis = state.elapsedMillis,
                    multiplier = state.liveMultiplier.toFloat(),
                    multiplierProgress = state.multiplierProgress,
                    checkpointedCurrency = state.checkpointedCurrency,
                    minutesSinceReset = state.minutesSinceReset,
                    isPaused = state.isPaused,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            // BOTTOM BUTTONS
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.xl),
                horizontalArrangement = Arrangement.spacedBy(64.dp)
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
                    containerColor = DarkTertiary,
                    onClick = viewModel::stopTracking
                )
            }
        }
    }
}