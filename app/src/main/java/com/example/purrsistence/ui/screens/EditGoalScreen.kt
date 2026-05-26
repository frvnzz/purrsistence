package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.ui.components.DeepFocusAccessibilityDialog
import com.example.purrsistence.ui.components.addEditGoal.DurationBox
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.clampDurationParts
import com.example.purrsistence.ui.util.durationPartsToMinutes
import com.example.purrsistence.ui.util.maxHourForGoalType
import com.example.purrsistence.ui.util.openAccessibilitySettings
import com.example.purrsistence.ui.util.requiresDeepFocusSetup
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import java.util.Locale

@Composable
fun EditGoalScreen(
    goalId: Int?,
    viewModel: GoalViewModel,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val context = LocalContext.current

    val goal by viewModel
        .getGoal(goalId)
        .collectAsState(initial = null)

    val showAccessibilityDialog = remember {
        mutableStateOf(false)
    }

    goal?.let { currentGoal ->

        var title by remember {
            mutableStateOf(currentGoal.title)
        }

        var type by remember {
            mutableStateOf(currentGoal.type.name.lowercase().replaceFirstChar {
                it.uppercase()
            })
        }

        val totalMinutes = currentGoal.targetDuration.toMinutes()

        var hours by remember {
            mutableStateOf(
                String.format(Locale.getDefault(), "%02d", totalMinutes / 60)
            )
        }

        var minutes by remember {
            mutableStateOf(
                String.format(Locale.getDefault(), "%02d", totalMinutes % 60)
            )
        }

        var deepFocus by remember {
            mutableStateOf(currentGoal.deepFocus)
        }

        val maxHours = maxHourForGoalType(type)

        LaunchedEffect(type) {
            val (safeHours, safeMinutes) = clampDurationParts(
                type = type,
                hours = hours,
                minutes = minutes
            )

            hours = safeHours
            minutes = safeMinutes
        }

        val durationInMinutes =
            (hours.toIntOrNull() ?: 0) * 60 +
                    (minutes.toIntOrNull() ?: 0)

        val titleValid = title.isNotBlank()
        val durationValid = durationInMinutes >= 1

        val formValid = titleValid && durationValid

        LaunchedEffect(Unit) {
            setTopBar(
                TopBarState(
                    title = "Edit Goal",
                    onBackClick = onBack
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { paneTitle = "Edit Goal Screen" }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = 100.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(
                    modifier = Modifier.height(Spacing.xxl)
                )

                // TITLE

                Text(
                    text = "Goal Title",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(Spacing.lg)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Goal Title") },
                    isError = !titleValid,
                    supportingText = {
                        if (!titleValid) {
                            Text("Goal title cannot be empty")
                        }
                    },
                    shape = Shapes.cards,
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.height(Spacing.xxl)
                )

                // TARGET DURATION

                Text(
                    text = "Target Duration",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(Spacing.xl)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {

                    DurationBox(
                        value = hours,
                        label = "Hour",
                        maxValue = maxHours,
                        onValueChange = { hours = it }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayLarge
                    )

                    DurationBox(
                        value = minutes,
                        label = "Minute",
                        maxValue = 59,
                        onValueChange = { minutes = it }
                    )
                }

                if (!durationValid) {
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "Duration must be at least 1 minute",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(
                    modifier = Modifier.height(Spacing.xxl)
                )

                // GOAL FREQUENCY

                Text(
                    text = "Goal Frequency",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(Spacing.lg)
                )

                Row(
                    modifier = Modifier.semantics { selectableGroup() },
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {

                    listOf("Daily", "Weekly").forEach { option ->

                        val selected = type == option

                        Button(
                            onClick = {
                                type = option
                            },

                            shape = Shapes.buttons,

                            modifier = Modifier.semantics {
                                this.selected = selected
                            },

                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selected) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                contentColor =
                                    if (selected) {
                                        MaterialTheme.colorScheme.onSecondary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                            )
                        ) {
                            Text(option)
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(Spacing.xxl)
                )

                // DEEP FOCUS

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = deepFocus,
                            onValueChange = {
                                deepFocus = it
                                if (
                                    requiresDeepFocusSetup(
                                        context = context,
                                        deepFocusEnabled = it
                                    )
                                ) {
                                    showAccessibilityDialog.value = true
                                }
                            },
                            role = Role.Switch
                        )
                        .semantics {
                            stateDescription = if (deepFocus) "Deep Focus on" else "Deep Focus off"
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Deep Focus",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Switch(
                            checked = deepFocus,

                            onCheckedChange = null, //handled by parent row for larger touch target and accessibility

                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,

                                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onPrimary,
                                uncheckedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(
                            modifier = Modifier.width(Spacing.sm)
                        )

                        Icon(
                            imageVector =
                                if (deepFocus) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },

                            contentDescription = null,

                            tint =
                                if (deepFocus) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceDim
                                }
                        )
                    }
                }
            }

            // BOTTOM BUTTONS

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Spacing.xl),

                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {

                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = Shapes.buttons,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val updatedMinutes = durationPartsToMinutes(
                            type = type,
                            hours = hours,
                            minutes = minutes
                        )
                        viewModel.updateGoal(
                            goalId = currentGoal.id,
                            title = title.trim(),
                            type = GoalType.valueOf(type.uppercase()),
                            hours = updatedMinutes,
                            deepFocus = deepFocus
                        )
                        onBack()
                    },

                    enabled = formValid,
                    modifier = Modifier.weight(1f),
                    shape = Shapes.buttons
                ) {

                    Text("Save")
                }
            }
        }

        if (showAccessibilityDialog.value) {
            DeepFocusAccessibilityDialog(
                onDismiss = {
                    showAccessibilityDialog.value = false
                },

                onOpenSettings = {
                    showAccessibilityDialog.value = false
                    openAccessibilitySettings(context)
                }
            )
        }
    }
}