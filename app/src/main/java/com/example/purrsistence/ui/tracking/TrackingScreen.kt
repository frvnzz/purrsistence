package com.example.purrsistence.ui.tracking

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onNavigateBackHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is TrackingEvent.NavigateBackHome) {
                onNavigateBackHome()
            }
        }
    }

    Column() {
        Text(text = formatDuration(state.elapsedMillis))

        Button(
            onClick = { viewModel.stopTracking() },
            enabled = state.isTracking
        ) {
            Text("Stop Tracking")
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}