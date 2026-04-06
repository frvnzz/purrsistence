package com.example.purrsistence.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.screens.AddGoalScreen
import com.example.purrsistence.ui.screens.EditGoalScreen
import com.example.purrsistence.ui.screens.GoalsScreen
import com.example.purrsistence.ui.screens.HomeScreen
import com.example.purrsistence.ui.tracking.TrackingEvent
import com.example.purrsistence.ui.tracking.TrackingScreen
import com.example.purrsistence.ui.tracking.TrackingViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    dataViewModel: DataViewModel,
    trackingViewModel: TrackingViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(Unit) {
        trackingViewModel.events.collect { event ->
            when (event) {
                TrackingEvent.NavigateToTrackingScreen -> {
                    navController.navigate("tracking")
                }
                TrackingEvent.NavigateBackHome -> {
                    navController.popBackStack("home", inclusive = false)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        // all screens go here :)
        // TODO: Maybe replace with single source of truth for routes (screen model) in the future

        // HOME
        composable("home") { HomeScreen(
            viewModel = dataViewModel,
            onStartTracking = { goalId, userId ->
                trackingViewModel.startTrack(goalId, userId)
            }
        ) }
        // GOALS
        composable("goals") {
            GoalsScreen(
                viewModel = dataViewModel,
                onAddGoalClick = {
                    navController.navigate("add_goal")
                },
                onGoalClick = { goalId ->
                    navController.navigate("edit_goal/$goalId")
                },
                snackbarHostState = snackbarHostState
            )
        }
        // -> edit goal
        composable("edit_goal/{goalId}") { backStackEntry ->
            val goalId = backStackEntry.arguments
                ?.getString("goalId")
                ?.toInt()

            EditGoalScreen(
                goalId = goalId,
                viewModel = dataViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        // -> add goal
        composable("add_goal") {
            AddGoalScreen(
                onSave = { title, type, minutes, deepFocus ->
                    dataViewModel.addGoal(
                        userId = 1,
                        title = title,
                        type = type,
                        weeklyMinutes = minutes,
                        deepFocus = deepFocus,
                        inactive = false, // inactive false per default
                        createdAt = System.currentTimeMillis(),
                        isCompleted = false
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        // TRACKING
        composable("tracking") {
            TrackingScreen(
                viewModel = trackingViewModel,
                onNavigateBackHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}