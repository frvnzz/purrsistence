package com.example.purrsistence.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.focus.DeepFocusAccessibilityState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EditGoalScreen(
    goalId: Int?,
    viewModel: GoalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val goal by viewModel.getGoal(goalId).collectAsState(initial = null)
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    goal?.let { currentGoal ->

        var title by remember { mutableStateOf(currentGoal.title) }
        var type by remember { mutableStateOf(currentGoal.type) }

        val initialHours = String.format(
            Locale.GERMANY,
            "%.1f",
            currentGoal.targetDuration.toMinutes() / 60.0
        )
        var hours by remember { mutableStateOf(initialHours) }

        var deepFocus by remember { mutableStateOf(currentGoal.deepFocus) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Edit Goal", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Goal Type")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily", "Weekly", "Monthly").forEach { option ->
                    val optionType = GoalType.valueOf(option.uppercase())

                    FilterChip(
                        selected = type == optionType,
                        onClick = { type = optionType },
                        label = { Text(option) }
                    )
                }
            }

            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text("Target Duration (hours)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deep Focus")
                Switch(
                    checked = deepFocus,
                    onCheckedChange = {
                        deepFocus = it
                        if (it && !DeepFocusAccessibilityState.isServiceEnabled(context)) {
                            showAccessibilityDialog = true
                        }
                    }
                )
            }

            if (showAccessibilityDialog) {
                AlertDialog(
                    onDismissRequest = { showAccessibilityDialog = false },
                    title = { Text("Enable Deep Focus Blocking") },
                    text = {
                        Text(
                            "To block other apps during Deep Focus, enable the accessibility service:\n\n" +
                                    "1. Tap Open Settings\n" +
                                    "2. Accessibility\n" +
                                    "3. Use Purrsistence\n" +
                                    "4. Turn it on"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAccessibilityDialog = false
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                } catch (_: ActivityNotFoundException) {
                                    context.startActivity(
                                        Intent(Settings.ACTION_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        ) {
                            Text("Open Settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAccessibilityDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Button(
                onClick = {
                    val normalized = hours.trim().replace(",", ".")
                    val hoursFloat = normalized.toFloatOrNull() ?: 0f
                    val hoursRounded = (hoursFloat * 10).roundToInt() / 10f
                    val minutes = (hoursRounded * 60).roundToInt()

                    viewModel.updateGoal(
                        goalId = currentGoal.id,
                        title = title,
                        type = type,
                        hours = minutes,
                        deepFocus = deepFocus
                    )

                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}