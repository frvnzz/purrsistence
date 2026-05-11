package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.purrsistence.ui.components.goalsScreen.GoalSearchBar
import com.example.purrsistence.ui.components.goalsScreen.DeleteGoalDialog
import com.example.purrsistence.ui.components.goalsScreen.DeleteGoalsButton
import com.example.purrsistence.ui.components.goalsScreen.GoalCard
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onAddGoalClick: () -> Unit = {},
    onGoalClick: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    setTopBar: (TopBarState) -> Unit
) {
    val goals by goalViewModel
        .searchedGoals(userViewModel.currentUserId)
        .collectAsState(initial = emptyList())

    val query = goalViewModel.searchQuery
    val isSearching = query.isNotBlank()

    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedGoals by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            goalViewModel.onSearchQueryChange("")
            isDeleteMode = false
            selectedGoals = emptySet()

            goalViewModel.resetCompletedGoalsIfNewCycle(userViewModel.currentUserId) //Reset completed goals at the start of a new timeframe
        }
    }

    // set TopBar content (header & delete button)
    setTopBar(
        TopBarState(
            title = "Your Goals",
            actions = if (goals.isNotEmpty()) {
                {
                    DeleteGoalsButton(
                        isDeleteMode = isDeleteMode,
                        selectedGoalsCount = selectedGoals.size,

                        onEnterDeleteMode = {
                            isDeleteMode = true
                        },

                        onCancel = {
                            isDeleteMode = false
                            selectedGoals = emptySet()
                        },

                        onDeleteClick = {
                            if (selectedGoals.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Select at least one goal to delete"
                                    )
                                }
                            } else {
                                showDeleteDialog = true
                            }
                        }
                    )
                }
            } else {
                null
            }
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
        ) {

            GoalSearchBar(
                query = goalViewModel.searchQuery,
                onQueryChange = goalViewModel::onSearchQueryChange
            )

            // GOAL CARDS LIST
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                contentPadding = PaddingValues(
                    top = Spacing.sm,
                    bottom = 64.dp
                )
            ) {
                if (goals.isEmpty()) {
                    item {
                        val message = if (isSearching) {
                            "No results for \"$query\""
                        } else {
                            "No goals yet - Add one! 🐱"
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(message)
                        }
                    }
                } else {
                    items(
                        items = goals,
                        key = { it.goal.id }
                    ) { goalWithSessions ->

                        val goal = goalWithSessions.goal
                        val isSelected = selectedGoals.contains(goal.id)

                        GoalCard(
                            goalWithSessions = goalWithSessions,
                            isDeleteMode = isDeleteMode,
                            isSelected = isSelected,
                            onClick = {
                                onGoalClick(goal.id)
                            },
                            onCheckedChange = { checked ->
                                selectedGoals = if (checked) {
                                    selectedGoals + goal.id
                                } else {
                                    selectedGoals - goal.id
                                }
                            }
                        )
                    }
                }
            }
        }

        // ADD GOAL BUTTON
        FloatingActionButton(
            onClick = onAddGoalClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = Elevation.Lvl4
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
        }

        if (showDeleteDialog) {
            DeleteGoalDialog(
                message = "Are you sure you want to delete ${selectedGoals.size} goals?",
                onConfirm = {
                    selectedGoals.forEach { goalViewModel.deleteGoal(it) }
                    selectedGoals = emptySet()
                    isDeleteMode = false
                    showDeleteDialog = false
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }
    }
}