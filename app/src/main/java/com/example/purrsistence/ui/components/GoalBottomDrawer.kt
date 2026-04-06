package com.example.purrsistence.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.data.local.relation.GoalWithSessions
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalBottomDrawer(
    goals: List<GoalWithSessions>,
    selectedGoalId: Int?,
    onGoalSelected: (Int) -> Unit,
    onStartClick: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    val selectedGoal = goals.find { it.goal.goalId == selectedGoalId }?.goal

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 80.dp,
        sheetDragHandle = null, // Removes the default handle pill to save vertical space
        sheetContent = {

            Column(modifier = Modifier.fillMaxWidth()) {

                // HANDLE / MINI PLAYER (Spotify style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Force height to match peek height exactly
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Visual distinction
                        .clickable {
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (goals.isEmpty()) "Create a Goal to track" else selectedGoal?.title ?: "Select a Goal",
                            style = MaterialTheme.typography.titleMedium
                        )
                        selectedGoal?.let {
                            val hours = it.targetDuration / 60f
                            val displayHours = String.format(Locale.GERMANY, "%.1f", hours)
                            Text(
                                "$displayHours h (${it.targetDuration} min)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            selectedGoal?.let { onStartClick(it.goalId) }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                }

                // EXPANDED LIST
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(goals) { goalWithSessions ->
                        val goal = goalWithSessions.goal
                        val isSelected = goal.goalId == selectedGoalId

                        val hours = goal.targetDuration / 60f
                        val displayHours =
                            String.format(Locale.GERMANY, "%.1f", hours)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.background
                                )
                                .clickable {
                                    onGoalSelected(goal.goalId)

                                    // collapse after selection
                                    scope.launch {
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium)
                            Text("Type: ${goal.type}")
                            Text("Duration: ${displayHours}h")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}