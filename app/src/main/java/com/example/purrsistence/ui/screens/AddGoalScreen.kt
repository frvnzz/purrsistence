package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun AddGoalScreen(
    onSave: (
        String,
        String,
        Int,
        Boolean
    ) -> Unit,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {

    val context = LocalContext.current

    var title by remember { mutableStateOf("New Goal") }
    var type by remember { mutableStateOf("Weekly") }
    var hours by remember { mutableStateOf("02") }
    var minutes by remember { mutableStateOf("30") }

    var deepFocus by remember { mutableStateOf(false) }

    val showAccessibilityDialog = remember {
        mutableStateOf(false)
    }

    val maxHours = maxHourForGoalType(type)
    LaunchedEffect(type) {
        val (safeHours, safeMinutes) = clampDurationParts(
            type = type,
            hours= hours,
            minutes = minutes
        )

        hours = safeHours
        minutes = safeMinutes
    }

    setTopBar(
        TopBarState(
            title = "Add New Goal",
            onBackClick = onBack
        )
    )

    val durationInMinutes =
        (hours.toIntOrNull() ?: 0) * 60 +
                (minutes.toIntOrNull() ?: 0)

    val titleValid = title.isNotBlank()
    val durationValid = durationInMinutes >= 1

    val formValid = titleValid && durationValid


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // TITLE

            Text(
                text = "Goal Title",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },

                modifier = Modifier.fillMaxWidth(),

                isError = !titleValid,

                supportingText = {
                    if (!titleValid) {
                        Text("Goal title cannot be empty")
                    }
                },

                placeholder = {
                    Text("New Goal")
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

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // TARGET DURATION
            Text(
                text = "Target Duration",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

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

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // GOAL FREQUENCY
            Text(
                text = "Goal Frequency",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                listOf("Daily", "Weekly").forEach { option ->

                    val selected = type == option

                    Button(
                        onClick = {
                            type = option
                        },

                        shape = Shapes.buttons,

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

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // DEEP FOCUS
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                        onCheckedChange = {
                            deepFocus = it
                            // pop up DeepFocusAlertDialog if turning on the switch
                            if (
                                requiresDeepFocusSetup(
                                    context = context,
                                    deepFocusEnabled = it
                                )
                            ) {
                                showAccessibilityDialog.value = true
                            }
                        },

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

            OutlinedButton(
                onClick = onBack,

                shape = Shapes.buttons,

                modifier = Modifier.weight(1f)
            ) {

                Text("Cancel")
            }

            Button(
                onClick = {

                    val totalMinutes = durationPartsToMinutes(
                        type = type,
                        hours = hours,
                        minutes = minutes
                    )

                    onSave(
                        title.trim(),
                        type,
                        totalMinutes,
                        deepFocus
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