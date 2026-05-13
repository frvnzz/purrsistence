package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.GoalWithSessions
import com.example.purrsistence.ui.theme.Spacing
import java.time.Instant
import java.util.Locale

// UI companion for the goals list sorting. Kept small and reusable.
enum class SortOption(val label: String) {
    LAST_TRACKED("Last tracked"),
    DATE_CREATED("Date created"),
    ALPHA_ASC("A → Z"),
    ALPHA_DESC("Z → A")
}

@Composable
fun GoalsSortMenu(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Sort:", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selectedSort.label)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open sort menu"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(SortOption.LAST_TRACKED.label) },
                    onClick = {
                        onSortChange(SortOption.LAST_TRACKED)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(SortOption.DATE_CREATED.label) },
                    onClick = {
                        onSortChange(SortOption.DATE_CREATED)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(SortOption.ALPHA_ASC.label) },
                    onClick = {
                        onSortChange(SortOption.ALPHA_ASC)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(SortOption.ALPHA_DESC.label) },
                    onClick = {
                        onSortChange(SortOption.ALPHA_DESC)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Pure helper to sort a list of GoalWithSessions according to the selected option.
fun sortGoals(goals: List<GoalWithSessions>, option: SortOption): List<GoalWithSessions> {
    return when (option) {
        SortOption.LAST_TRACKED -> goals.sortedByDescending { gw ->
            gw.sessions.maxOfOrNull { it.endTime ?: it.startTime } ?: Instant.EPOCH
        }

        SortOption.DATE_CREATED -> goals.sortedByDescending { gw ->
            gw.goal.createdAt
        }

        SortOption.ALPHA_ASC -> goals.sortedBy { gw ->
            gw.goal.title.lowercase(Locale.getDefault())
        }

        SortOption.ALPHA_DESC -> goals.sortedByDescending { gw ->
            gw.goal.title.lowercase(Locale.getDefault())
        }
    }
}
