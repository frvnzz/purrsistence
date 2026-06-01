package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.state.StatisticsUiState
import com.example.purrsistence.ui.util.getWeekDisplay
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel

@Composable
fun WeekSelector(
    viewModel: StatisticsViewModel,
    state: StatisticsUiState,
    compact: Boolean = false
) {
    val (label, dateRange) = getWeekDisplay(state.weekOffset)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 0.dp else 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous week button
        IconButton(
            onClick = { viewModel.previousWeek() },
            modifier = if (compact) Modifier.size(32.dp) else Modifier
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week"
            )
        }

        // Week info (centered)
        if (compact) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Right-side controls: optional "Back to this week" and the next-week arrow
        WeekRightControls(viewModel = viewModel, state = state, compact = compact)
    }
}

@Composable
private fun WeekRightControls(
    viewModel: StatisticsViewModel,
    state: StatisticsUiState,
    compact: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (state.weekOffset < 0) {
            // Show a compact action to quickly return to the current week when viewing past weeks
            IconButton(
                onClick = { viewModel.jumpToThisWeek() },
                modifier = if (compact) Modifier.size(32.dp) else Modifier
            ) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = "Back to this week",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Next week button (used to step forward, disabled when already at current week)
        IconButton(
            enabled = state.weekOffset < 0,
            onClick = { viewModel.nextWeek() },
            modifier = if (compact) Modifier.size(32.dp) else Modifier
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next week"
            )
        }
    }
}