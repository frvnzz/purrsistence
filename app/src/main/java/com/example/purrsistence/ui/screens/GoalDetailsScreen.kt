package com.example.purrsistence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.DeepFocusAccessibilityDialog
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatLocalizedInteger
import com.example.purrsistence.ui.util.formatMinutesForAccessibility
import com.example.purrsistence.ui.util.handleStartTrackingClick
import com.example.purrsistence.ui.util.openAccessibilitySettings
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun GoalDetailsScreen(
    goalId: Int?,
    goalViewModel: GoalViewModel,
    onEditClick: (Int) -> Unit,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit,
    onStartTracking: (Int, String, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val goalWithSessions by goalViewModel
        .getGoalWithSessions(goalId)
        .collectAsState(initial = null)

    val currentGoalWithSessions = goalWithSessions ?: return
    val currentGoal = currentGoalWithSessions.goal

    // format targetDuration of the goal
    val totalMinutesDuration = currentGoal.targetDuration.toMinutes()
    val displayHours = totalMinutesDuration / 60
    val displayMinutes = totalMinutesDuration % 60

    val formattedDuration =
        if (displayMinutes == 0L) {
            "${formatLocalizedInteger(displayHours.toInt())}h"
        } else {
            "${formatLocalizedInteger(displayHours.toInt())}h ${formatLocalizedInteger(displayMinutes.toInt())}m"
        }

    val progress = currentGoalWithSessions.currentProgress(ZonedDateTime.now())

    // format type of the goal
    val formattedType =
        currentGoal.type
            .name
            .lowercase(Locale.ROOT)
            .replaceFirstChar {
                it.titlecase(Locale.ROOT)
            }

    LaunchedEffect(currentGoal.id) {
        setTopBar(
            TopBarState(
                title = "Goal Details",
                onBackClick = onBack,
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onEditClick(currentGoal.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Goal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Goal",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Goal Details Screen" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // HEADER SECTION
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = currentGoal.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = formattedType,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                        )
                    }
                    
                    if (currentGoal.deepFocus) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.height(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Deep Focus",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // INFO SECTION
            SettingsSection(title = "Goal Information") {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    InfoItem(
                        icon = Icons.Default.Schedule,
                        label = "Target Duration",
                        value = formattedDuration,
                        contentDescription = "Target Duration: ${formatMinutesForAccessibility(totalMinutesDuration.toInt())}"
                    )
                    
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    InfoItem(
                        icon = Icons.Default.Visibility,
                        label = "Deep Focus Mode",
                        value = if (currentGoal.deepFocus) "Enabled" else "Disabled"
                    )
                }
            }

            // PROGRESS SECTION
            SettingsSection(title = "Current Progress") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics {
                                contentDescription = if (currentGoal.isCompleted) {
                                    "Progress: Completed"
                                } else {
                                    "Progress: ${(progress * 100).toInt()}%"
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(CircleShape)
                                    .background(color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.md))

                        if (currentGoal.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Text(
                        text = if (currentGoal.isCompleted) "Goal reached! Purr-fect job!" else "Keep going to reach your goal!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // START TRACKING BUTTON
        Button(
            onClick = {
                handleStartTrackingClick(
                    goal = currentGoal,
                    context = context,
                    onStartTracking = onStartTracking,
                    onNeedsAccessibilitySetup = {
                        showAccessibilityDialog = true
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(Spacing.lg),
            shape = Shapes.buttons,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = Elevation.Lvl2
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Start Tracking", style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal?") },
            text = { Text("This will permanently remove '${currentGoal.title}'. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        goalViewModel.deleteGoal(currentGoal.id)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = Shapes.buttons
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    shape = Shapes.buttons
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAccessibilityDialog) {
        DeepFocusAccessibilityDialog(
            onDismiss = { showAccessibilityDialog = false },
            onOpenSettings = {
                showAccessibilityDialog = false
                openAccessibilitySettings(context)
            }
        )
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    contentDescription: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) this.contentDescription = contentDescription
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.lg))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
