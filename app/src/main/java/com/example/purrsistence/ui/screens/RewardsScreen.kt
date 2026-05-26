package com.example.purrsistence.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.example.purrsistence.ui.components.tracking.TrackingActionButton
import com.example.purrsistence.ui.theme.DarkTertiary
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatDuration
import com.example.purrsistence.ui.util.formatLocalizedDecimal
import com.example.purrsistence.ui.util.formatLocalizedInteger
import com.example.purrsistence.ui.viewmodel.TrackingViewModel

@Composable
fun RewardsScreen(
    viewModel: TrackingViewModel,
    onReturnHome: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // get the session duration (without paused duration)
    val effectiveSessionMillis = state.sessionDurationMillis ?: 0L

    if (isLandscape) {
        // LANDSCAPE
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = state.goalTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                Text(
                    text = "+${formatLocalizedInteger(state.rewardedCurrency ?: 0)} coins",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = formatDuration(
                        effectiveSessionMillis.coerceAtLeast(0L)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "Multiplier x${formatLocalizedDecimal(state.multiplier ?: 1.0, 2)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                // Give extra reward if the goal is completed (once)
                if ((state.goalCompletionReward ?: 0) > 0) {
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Text(
                        text = "GOAL COMPLETED",
                        style = MaterialTheme.typography.titleLarge,
                        color = DarkTertiary
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "+${formatLocalizedInteger(state.goalCompletionReward ?: 0)} bonus coins",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.6f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrackingActionButton(
                    text = "Return Home",
                    onClick = onReturnHome
                )
            }
        }

    } else {
        // PORTRAIT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl)
        ) {

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = state.goalTitle,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                Text(
                    text = "+${formatLocalizedInteger(state.rewardedCurrency ?: 0)} coins",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = formatDuration(
                        effectiveSessionMillis.coerceAtLeast(0L)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "Multiplier x${formatLocalizedDecimal(state.multiplier ?: 1.0, 2)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                if ((state.goalCompletionReward ?: 0) > 0) {

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Text(
                        text = "GOAL COMPLETED",
                        style = MaterialTheme.typography.titleLarge,
                        color = DarkTertiary
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "+${formatLocalizedInteger(state.goalCompletionReward ?: 0)} bonus coins",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            TrackingActionButton(
                text = "Return Home",
                onClick = onReturnHome,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.xl)
            )
        }
    }
}