package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.purrsistence.domain.model.GoalStat
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatMinutes
import com.example.purrsistence.ui.util.formatMinutesForAccessibility

@Composable
fun GoalStatsList(goals: List<GoalStat>) {

    val max = goals.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(1) ?: 1

    Column {
        Text(
            text = "Tracked Time per Goal",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = Spacing.sm)
                .semantics { heading() }
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            goals.forEach { goal ->
                val progress = goal.totalMinutes / max.toFloat()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            contentDescription = "${goal.goalName}: ${formatMinutesForAccessibility(goal.totalMinutes)}"
                        }
                        .padding(vertical = Spacing.xs)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = goal.goalName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatMinutes(goal.totalMinutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xxs))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
            }
        }
    }
}