package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.example.purrsistence.domain.model.GoalStat
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatMinutes
import com.example.purrsistence.ui.util.formatMinutesForAccessibility

@Composable
fun GoalStatsList(goals: List<GoalStat>) {

    val max = goals.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(1) ?: 1

    Column{
        Text(
            text = "Tracked Time per Goal",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics { heading() }
        )

        Column {
            goals.forEach { goal ->
                val progress = goal.totalMinutes / max.toFloat()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            contentDescription = "${goal.goalName}: ${formatMinutesForAccessibility(goal.totalMinutes)}"
                        }
                        .padding(Spacing.md)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(goal.goalName)
                        Text(formatMinutes(goal.totalMinutes))
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProgressIndicatorDefaults.linearColor,
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
            }
        }
    }
}