package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.components.DeepFocusAccessibilityDialog
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.homeScreen.GoalBottomDrawer
import com.example.purrsistence.ui.util.handleStartTrackingClick
import com.example.purrsistence.ui.util.openAccessibilitySettings
import com.example.purrsistence.ui.viewmodel.UserViewModel
import com.example.purrsistence.service.RoomService
import com.example.purrsistence.ui.components.homeScreen.RoomView
import com.example.purrsistence.ui.components.homeScreen.CatSelectionDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.homeScreen.SelectCatsButton
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onStartTracking: (Int, String, Int, Boolean) -> Unit,
    setTopBar: (TopBarState) -> Unit
) {

    val context = LocalContext.current
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showCatSelectionDialog by remember { mutableStateOf(false) }

    val user by userViewModel.user.collectAsState()
    val balance = user?.balance ?: 0
    val collectedCats = user?.collectedCatsIds ?: emptyList()
    val selectedCatIds = user?.selectedCatIds ?: emptyList()

    val roomService = remember { RoomService() }
    val spots = remember { roomService.getRoomSpots() }

    val catsToDisplay = remember(collectedCats, selectedCatIds) {
        selectedCatIds.ifEmpty { collectedCats.take(5) }
    }

    val placedCats = remember(catsToDisplay, spots) {
        roomService.assignCatsToSpots(catsToDisplay, spots)
    }

    val goals by goalViewModel.goals(1).collectAsState(initial = emptyList())
    val selectedGoalId = goalViewModel.selectedGoalId

    LaunchedEffect(goals) {
        if (selectedGoalId == null && goals.isNotEmpty()) {
            goalViewModel.selectGoal(goals.first().goal.id)
        }
    }
    val selectedGoal = goals.find { it.goal.id == selectedGoalId }?.goal

    // set TopBar content (header & CurrencyBadge)
    setTopBar(
        TopBarState(
            title = "Your Cats",
            actions = { CurrencyBadge(balance = balance) }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = Spacing.lg,
                        top = Spacing.lg,
                        end = Spacing.lg,
                        bottom = 100.dp // same as collapsedHeight from GoalBottomDrawer
                    )
            ) {
                SelectCatsButton(
                    onClick = { showCatSelectionDialog = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = Spacing.md)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    RoomView(
                        placedCats = placedCats,
                        spots = spots
                    )
                }
            }
        }

        // GOAL PICKER / DRAWER OVERLAY
        GoalBottomDrawer(
            goals = goals,
            selectedGoalId = selectedGoalId,
            onGoalSelected = { goalViewModel.selectGoal(it) },
            onStartClick = { _, _ ->
                handleStartTrackingClick(
                    goal = selectedGoal,
                    context = context,
                    onStartTracking = onStartTracking,
                    onNeedsAccessibilitySetup = { showAccessibilityDialog = true }
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showAccessibilityDialog) {
            DeepFocusAccessibilityDialog(
                onDismiss = { showAccessibilityDialog = false },
                onOpenSettings = {
                    showAccessibilityDialog = false
                    openAccessibilitySettings(context)
                }
            )
        }

        if (showCatSelectionDialog) {
            CatSelectionDialog(
                collectedCatIds = collectedCats,
                initiallySelectedIds = selectedCatIds,
                onDismiss = { showCatSelectionDialog = false },
                onConfirm = {
                    userViewModel.updateSelectedCats(it)
                    showCatSelectionDialog = false
                }
            )
        }
    }
}