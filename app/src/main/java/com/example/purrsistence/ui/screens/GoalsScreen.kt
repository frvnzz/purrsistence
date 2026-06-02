package com.example.purrsistence.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.purrsistence.ui.components.goalsScreen.DeleteGoalDialog
import com.example.purrsistence.ui.components.goalsScreen.DeleteGoalsButton
import com.example.purrsistence.ui.components.goalsScreen.GoalCard
import com.example.purrsistence.ui.components.goalsScreen.GoalSearchBar
import com.example.purrsistence.ui.components.goalsScreen.GoalsEmptyState
import com.example.purrsistence.ui.components.goalsScreen.GoalsSortMenu
import com.example.purrsistence.ui.components.goalsScreen.SortOption
import com.example.purrsistence.ui.components.goalsScreen.sortGoals
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
    onStartTracking: (Int, String, Int, Boolean) -> Unit = { _, _, _, _ -> },
    snackbarHostState: SnackbarHostState,
    setTopBar: (TopBarState) -> Unit
) {
    val goals by goalViewModel
        .searchedGoals(userViewModel.currentUserId)
        .collectAsState(initial = emptyList())

    val query = goalViewModel.searchQuery
    val isSearching = query.isNotBlank()

    val focusManager = LocalFocusManager.current

    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedGoals by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var swipeToDeleteGoalId by remember { mutableStateOf<Int?>(null) }
    var selectedSort by remember { mutableStateOf(SortOption.LAST_TRACKED) }

    // Goal List state
    val gridState = rememberLazyGridState()
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    // columns for Goals List (portrait or landscape)
    val columns = if (isLandscape) 2 else 1

    var pendingVisibleGoalId by remember { mutableStateOf<Int?>(null) }
    var pendingVisibleScrollOffset by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current

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
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Your Goals Screen" }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
        ) {
            // currently visible item when the user changes sort (to preserve scroll position)
            val displayGoals = remember(goals, selectedSort) {
                sortGoals(goals, selectedSort)
            }

            // SEARCH BAR AND SORT MENU IN A SINGLE ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GoalSearchBar(
                        query = goalViewModel.searchQuery,
                        onQueryChange = goalViewModel::onSearchQueryChange
                    )
                }

                GoalsSortMenu(
                    selectedSort = selectedSort,
                    onSortChange = { newSort ->
                        // capture currently visible goal id + offset so we can restore view after reordering
                        pendingVisibleGoalId =
                            displayGoals.getOrNull(gridState.firstVisibleItemIndex)?.goal?.id
                        pendingVisibleScrollOffset =
                            gridState.firstVisibleItemScrollOffset
                        selectedSort = newSort
                    }
                )
            }

            // GOAL CARDS LIST (sorted according to selectedSort)
            LaunchedEffect(selectedSort) {
                pendingVisibleGoalId?.let { id ->
                    val idx =
                        displayGoals.indexOfFirst {
                            it.goal.id == id
                        }
                    if (idx != -1) {
                        gridState.scrollToItem(
                            index = idx,
                            scrollOffset = pendingVisibleScrollOffset
                        )
                    } else {
                        gridState.scrollToItem(0)
                    }
                    pendingVisibleGoalId = null
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(
                    Spacing.lg
                ),
                contentPadding = PaddingValues(
                    top = Spacing.sm,
                    bottom = Spacing.xxl
                )
            ) {
                // GoalsEmptyState
                if (displayGoals.isEmpty()) {
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        GoalsEmptyState(
                            isSearching = isSearching,
                            query = query,
                            onAddGoalClick = onAddGoalClick
                        )
                    }

                } else {
                    items(
                        items = displayGoals,
                        key = { it.goal.id }
                    ) { goalWithSessions ->

                        val goal = goalWithSessions.goal

                        val isSelected =
                            selectedGoals.contains(goal.id)

                        GoalCard(
                            goalWithSessions = goalWithSessions,
                            isDeleteMode = isDeleteMode,
                            isSelected = isSelected,
                            onClick = {
                                onGoalClick(goal.id)
                            },
                            onCheckedChange = { checked ->
                                selectedGoals =
                                    if (checked) {
                                        selectedGoals + goal.id
                                    } else {
                                        selectedGoals - goal.id
                                    }
                            },
                            onTrack = {
                                onStartTracking(
                                    goal.id,
                                    goal.title,
                                    goal.userId,
                                    goal.deepFocus
                                )
                            },
                            onDelete = {
                                swipeToDeleteGoalId = goal.id
                            }
                        )
                    }
                }
            }
        }

        // ADD GOAL BUTTON (positioned in the Box, not in the Column)
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

        if (swipeToDeleteGoalId != null) {
            val goalToDelete = goals.find { it.goal.id == swipeToDeleteGoalId }?.goal
            DeleteGoalDialog(
                title = "Delete Goal",
                message = "Are you sure you want to delete \"${goalToDelete?.title}\"?",
                onConfirm = {
                    swipeToDeleteGoalId?.let { goalViewModel.deleteGoal(it) }
                    swipeToDeleteGoalId = null
                },
                onDismiss = {
                    swipeToDeleteGoalId = null
                }
            )
        }
    }
}
