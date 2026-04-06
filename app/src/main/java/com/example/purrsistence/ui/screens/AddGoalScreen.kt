package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AddGoalScreen(
    onSave: (
        String,
        String,
        Int,
        Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Weekly") }
    var hours by remember { mutableStateOf("") }
    var deepFocus by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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

        // Save Button
        Button(
            onClick = {
                val normalized = hours.trim().replace(",", ".")
                val hoursFloat = normalized.toFloatOrNull() ?: 0f
                val minutes = (hoursFloat * 60f).roundToInt()

                onSave(
                    title,
                    type,
                    minutes,
                    deepFocus
                )
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goal")
        }
    }
}