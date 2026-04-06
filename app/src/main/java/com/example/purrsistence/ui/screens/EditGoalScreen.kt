package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.DataViewModel
import kotlin.math.roundToInt

@Composable
fun EditGoalScreen(
    goalId: Int?,
    viewModel: DataViewModel,
    onBack: () -> Unit
) {
    val goal by viewModel.getGoal(goalId).collectAsState(initial = null)

    goal?.let { currentGoal ->

        // Pre-filled state
        var title by remember { mutableStateOf(currentGoal.title) }
        var type by remember { mutableStateOf(currentGoal.type) }

        // convert minutes -> hours string
        val initialHours = (currentGoal.targetDuration / 60f).toString()
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

            // TITLE
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // TYPE
            Text("Goal Type")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily", "Weekly", "Monthly").forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option) }
                    )
                }
            }

            // HOURS
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text("Target Duration (hours)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // DEEP FOCUS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deep Focus")
                Switch(
                    checked = deepFocus,
                    onCheckedChange = { deepFocus = it }
                )
            }

            // SAVE
            Button(
                onClick = {
                    val normalized = hours.trim().replace(",", ".")
                    val hoursFloat = normalized.toFloatOrNull() ?: 0f
                    val minutes = (hoursFloat * 60f).roundToInt()

                    viewModel.updateGoal(
                        goalId = currentGoal.goalId,
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