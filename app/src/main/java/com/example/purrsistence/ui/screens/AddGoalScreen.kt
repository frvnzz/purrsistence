package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddGoalScreen(
    onSave: (
        String,
        String,
        Int,
        Boolean,
        Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Weekly") }
    var hours by remember { mutableStateOf("") }
    var deepFocus by remember { mutableStateOf(false) }
    var inactive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Add Goal", style = MaterialTheme.typography.titleLarge)

        // title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Goal Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // type (Daily / Weekly / Monthly)
        Text("Goal Type")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Daily", "Weekly", "Monthly").forEach { option ->
                FilterChip(
                    selected = type == option,
                    onClick = { type = option },
                    label = { Text(option) }
                )
            }
        }

        // Duration (in hours, but saved in minutes)
        OutlinedTextField(
            value = hours,
            onValueChange = { hours = it },
            label = { Text("Target Duration (hours)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Deep Focus
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

        // Inactive ??
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Inactive")
            Switch(
                checked = inactive,
                onCheckedChange = { inactive = it }
            )
        }

        // Save Button
        Button(
            onClick = {
                val normalized = hours.replace(",", ".")
                val minutes = ((normalized.toFloatOrNull() ?: 0f) * 60f).toInt()

                onSave(
                    title,
                    type,
                    minutes,
                    deepFocus,
                    inactive
                )
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goal")
        }
    }
}