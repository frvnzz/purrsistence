package com.example.purrsistence.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.screens.AddGoalScreen
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
    modifier: Modifier = Modifier
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
                }
            )
        }
        // -> add goal
        composable("add_goal") {
            AddGoalScreen(
                onSave = { title, type, minutes, deepFocus, inactive ->
                    dataViewModel.addGoal(
                        userId = 1,
                        title = title,
                        type = type,
                        weeklyMinutes = minutes,
                        deepFocus = deepFocus,
                        inactive = inactive,
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