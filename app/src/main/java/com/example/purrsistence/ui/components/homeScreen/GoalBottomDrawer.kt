package com.example.purrsistence.ui.components.homeScreen

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
import com.example.purrsistence.domain.model.GoalWithSessions
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

    val selectedGoal = goals.find { it.goal.id == selectedGoalId }?.goal

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 80.dp,
        sheetDragHandle = null,
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            if (goals.isEmpty()) "Create a Goal to track"
                            else selectedGoal?.title ?: "Select a Goal",
                            style = MaterialTheme.typography.titleMedium
                        )

                        selectedGoal?.let {
                            val totalMinutes = it.targetDuration.toMinutes()
                            val displayHours = String.format(
                                Locale.GERMANY,
                                "%.1f",
                                totalMinutes / 60.0
                            )

                            Text(
                                "$displayHours h (${totalMinutes} min)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            selectedGoal?.let { onStartClick(it.id) }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(goals) { goalWithSessions ->
                        val goal = goalWithSessions.goal
                        val isSelected = goal.id == selectedGoalId

                        val totalMinutes = goal.targetDuration.toMinutes()
                        val displayHours = String.format(
                            Locale.GERMANY,
                            "%.1f",
                            totalMinutes / 60.0
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    }
                                )
                                .clickable {
                                    onGoalSelected(goal.id)
                                    scope.launch {
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium)
                            Text("Type: ${goal.type}")
                            Text("Duration: ${displayHours}h (${totalMinutes} min)")
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