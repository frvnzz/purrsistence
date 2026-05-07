package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.purrsistence.data.local.relation.GoalWithSessionsEntity
import com.example.purrsistence.domain.model.GoalWithSessions
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun GoalCard(
    goalWithSessions: GoalWithSessions,
    isEditMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val goal = goalWithSessions.goal
    val now = ZonedDateTime.now()
    val progress = goalWithSessions.currentProgress(now)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isEditMode) Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium)

                if (goal.isCompleted) { //show a checkmark icon if the goal is completed
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Goal Completed",
                        tint = Color(0xFF4CAF50), // Green color for completed goals
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text("Type: ${goal.type}")

                val minutes = goal.targetDuration.toMinutes().toInt()
                val hoursFloat = minutes / 60f
                val displayHours = String.format(Locale.GERMANY, "%.1f", hoursFloat)

                val trackedMinutes = goalWithSessions.totalTrackedDuration().toMinutes()
                val trackedHours = trackedMinutes / 60
                val trackedRemainderMinutes = trackedMinutes % 60

                Text("Duration: ${displayHours}h (${minutes} min)")
                Text("Tracked: ${trackedHours}h ${trackedRemainderMinutes}min")

                Text("Deep Focus: ${if (goal.deepFocus) "ON" else "OFF"}")
                Text("Inactive: ${if (goal.inactive) "YES" else "NO"}")
                Text("Created: ${goal.createdAt}")
                Text("Completed: ${if (goal.isCompleted) "YES" else "NO"}")

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
                Text("${(progress * 100).toInt()}%")
            }

            if (isEditMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}