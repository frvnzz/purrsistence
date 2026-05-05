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
import com.example.purrsistence.domain.model.GoalStat
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun GoalStatsList(goals: List<GoalStat>) {

    val max = goals.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(1) ?: 1

    Column{
        Text("Tracked Time per Goal", style = MaterialTheme.typography.labelLarge)

        Column {
            goals.forEach { goal ->
                val progress = goal.totalMinutes / max.toFloat()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(goal.goalName)
                        Text("${goal.totalMinutes} min")
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