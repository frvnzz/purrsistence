package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.GoalWithSessions
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatLocalizedInteger
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun GoalCard(
    goalWithSessions: GoalWithSessions,
    isDeleteMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit = {},
    onTrack: () -> Unit = {}
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
            "${formatLocalizedInteger(displayHours.toInt())}h ${
                formatLocalizedInteger(
                    displayMinutes.toInt()
                )
            }m"
        }

    val progress = goalWithSessions.currentProgress(ZonedDateTime.now())

    val formattedType =
        goal.type
            .name
            .lowercase(Locale.ROOT)
            .replaceFirstChar {
                it.titlecase(Locale.ROOT)
            }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDelete()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onTrack()
                    false
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isDeleteMode,
        enableDismissFromEndToStart = !isDeleteMode,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiary
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primary
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.PlayArrow
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(Shapes.cards)
                    .background(color)
                    .padding(horizontal = Spacing.lg),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    val selectionStatus = if (isDeleteMode) {
                        if (isSelected) ", selected" else ", not selected"
                    } else ""
                    val completionStatus =
                        if (goal.isCompleted) ", completed" else ", ${(progress * 100).toInt()}% progress"

                    contentDescription =
                        "${goal.title}, $formattedDuration $formattedType$completionStatus$selectionStatus"
                    role = Role.Button
                }
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
}
