package com.example.purrsistence.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.purrsistence.service.RoomService
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.DeepFocusAccessibilityDialog
import com.example.purrsistence.ui.components.homeScreen.CatSelectionDialog
import com.example.purrsistence.ui.components.homeScreen.GoalBottomDrawer
import com.example.purrsistence.ui.components.homeScreen.RoomView
import com.example.purrsistence.ui.components.homeScreen.SelectCatsButton
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.SoundManager
import com.example.purrsistence.ui.util.handleStartTrackingClick
import com.example.purrsistence.ui.util.openAccessibilitySettings
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onStartTracking: (Int, String, Int, Boolean) -> Unit,
    setTopBar: (TopBarState) -> Unit,
    soundManager: SoundManager
) {
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
            title = "Home",
            actions = { CurrencyBadge(balance = balance) }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Home Screen" }
    ) {

        // MAIN CONTENT
        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        spots = spots,
                        onCatTap = { soundManager.playMeow() }
                    )
                }
            }
        }

        // GOAL PICKER / DRAWER OVERLAY
        GoalBottomDrawer(
            modifier = Modifier.align(Alignment.BottomCenter),
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
            alwaysExpanded = isLandscape
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