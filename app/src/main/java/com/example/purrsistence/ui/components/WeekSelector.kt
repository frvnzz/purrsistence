package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
            .padding(vertical = if (compact) 4.dp else 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous week button
        IconButton(onClick = { viewModel.previousWeek() }) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week"
            )
        }

        // Week info (centered)
        if (compact) {
            Text(
                text = "$label · $dateRange",
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.weekOffset < 0) {
                // Show a compact action to quickly return to the current week when viewing past weeks
                TextButton(
                    onClick = { viewModel.jumpToThisWeek() },
                    modifier = Modifier.padding(end = if (compact) 4.dp else 8.dp)
                ) {
                    Text(
                        text = "Back to this week",
                        style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Next week button (used to step forward, disabled when already at current week)
            IconButton(
                enabled = state.weekOffset < 0,
                onClick = { viewModel.nextWeek() }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next week"
                )
            }
        }
    }
}