package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.components.GoalBottomDrawer

@Composable
fun HomeScreen(
    viewModel: DataViewModel,
    onStartTracking: (Int, Int) -> Unit
) {
    val goals by viewModel.goals(1).collectAsState(initial = emptyList())

    // Use ViewModel state so that user can switch between screens and selectedGoalId is remembered
    val selectedGoalId = viewModel.selectedGoalId

    // Auto-select first goal if none is selected
    LaunchedEffect(goals) {
        if (selectedGoalId == null && goals.isNotEmpty()) {
            viewModel.selectGoal(goals.first().goal.goalId)
        }
    }

    val selectedGoal = goals.find { it.goal.goalId == selectedGoalId }?.goal

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        GoalBottomDrawer(
            goals = goals,
            selectedGoalId = selectedGoalId,
            onGoalSelected = { viewModel.selectGoal(it) },
            onStartClick = {
                selectedGoal?.let {
                    onStartTracking(it.goalId, it.targetDuration)
                }
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Welcome Home!", style = MaterialTheme.typography.titleLarge)
                }
                // Cat UI can go here later :)
            }
        }
    }
}