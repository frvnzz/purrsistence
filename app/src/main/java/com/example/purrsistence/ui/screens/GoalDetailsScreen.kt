package com.example.purrsistence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.DeepFocusAccessibilityDialog
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
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

    val goalWithSessions by goalViewModel
        .getGoalWithSessions(goalId)
        .collectAsState(initial = null)

    val currentGoalWithSessions = goalWithSessions ?: return
    val currentGoal = currentGoalWithSessions.goal

    // formate targetDuration of the goal
    val totalMinutesDuration = currentGoal.targetDuration.toMinutes()
    val displayHours = totalMinutesDuration / 60
    val displayMinutes = totalMinutesDuration % 60

    val formattedDuration =
        if (displayMinutes == 0L) {
            "${displayHours}h"
        } else {
            "${displayHours}h ${displayMinutes}m"
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

    LaunchedEffect(Unit) {
        setTopBar(
            TopBarState(
                title = "Goal Details",
                onBackClick = onBack
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Goal Details Screen" }
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {

        ElevatedCard(
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
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentGoal.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // TODO: Refactor UI so that the edit (+ delete?) button is right next to the Title (circular buttons)

                    if (currentGoal.deepFocus) {

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Deep Focus enabled",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(Spacing.sm)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {

                    Text(
                        text = "Goal Information",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )

                    Text(
                        text = "Target Duration: $formattedDuration",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            contentDescription = "Target Duration: ${formatMinutesForAccessibility(totalMinutesDuration.toInt())}"
                        }
                    )

                    Text(
                        text = "Cycle: $formattedType",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            contentDescription = "Goal Cycle: $formattedType"
                        }
                    )

                    Text(
                        text = if (currentGoal.deepFocus) {
                            "Deep Focus enabled"
                        } else {
                            "Deep Focus disabled"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Current Progress",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )

                    // PROGRESS BAR
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
                                .height(8.dp)
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

                        if (currentGoal.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium
                            )
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.buttons,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = Elevation.Lvl1
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(Spacing.sm)
                        )

                        Text("Start Tracking")
                    }
                }
            }
        }

        // EDIT BUTTON
        Button(
            onClick = {
                onEditClick(currentGoal.id)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.buttons,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = Elevation.Lvl1
            )
        ) {

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(Spacing.sm)
            )

            Text("Edit Goal")
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
}