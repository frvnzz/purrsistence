package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.DataViewModel

@Composable
fun EditGoalScreen(
    goalId: Int?,
    viewModel: DataViewModel,
    onBack: () -> Unit
) {
    val goal by viewModel.getGoal(goalId).collectAsState(initial = null)

    goal?.let { currentGoal ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var title by remember { mutableStateOf(currentGoal.title) }
            var hours by remember { mutableIntStateOf(currentGoal.targetDuration) }

            Text("Edit Goal", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") }
            )

            Button(
                onClick = {
                    viewModel.updateGoal(currentGoal.goalId, title, hours)
                    onBack()
                }
            ) {
                Text("Save")
            }
        }
    }
}