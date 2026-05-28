package com.example.purrsistence.ui.components.goalsScreen

import java.time.ZonedDateTime
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.GoalWithSessions
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatLocalizedInteger

@Composable
fun GoalCard(
    goalWithSessions: GoalWithSessions,
    isDeleteMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val goal = goalWithSessions.goal

    // format targetDuration of the goal
    val totalMinutesDuration = goal.targetDuration.toMinutes()
    val displayHours = totalMinutesDuration / 60
    val displayMinutes = totalMinutesDuration % 60

    val formattedDuration =
        if (displayMinutes == 0L) {
            "${formatLocalizedInteger(displayHours.toInt())}h"
        } else {
            "${formatLocalizedInteger(displayHours.toInt())}h ${formatLocalizedInteger(displayMinutes.toInt())}m"
        }

    val progress = goalWithSessions.currentProgress(ZonedDateTime.now())

    val formattedType =
        goal.type
            .name
            .lowercase(Locale.ROOT)
            .replaceFirstChar {
                it.titlecase(Locale.ROOT)
            }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDeleteMode) {
                    Modifier.clickable { onCheckedChange(!isSelected) }
                } else {
                    Modifier.clickable { onClick() }
                }
            ),
        shape = Shapes.cards,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = Elevation.Lvl2
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // TEXT (Goal title & targetDuration)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "$formattedDuration $formattedType",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // DEEP FOCUS ICON
                if (goal.deepFocus) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Deep Focus",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = Spacing.sm)
                    )
                }
                // CHECKBOX (bulk delete)
                if (isDeleteMode) {
                    Checkbox(
                        modifier = Modifier.height(Spacing.sm),
                        checked = isSelected,
                        onCheckedChange = onCheckedChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // GOAL PROGRESS BAR
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(Shapes.buttons)
                        .background(color = MaterialTheme.colorScheme.surfaceDim)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(Shapes.buttons)
                            .background(color = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                if (goal.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}