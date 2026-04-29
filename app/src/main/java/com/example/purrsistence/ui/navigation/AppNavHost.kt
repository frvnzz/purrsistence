package com.example.purrsistence.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.screens.AddGoalScreen
import com.example.purrsistence.ui.screens.EditGoalScreen
import com.example.purrsistence.ui.screens.GoalsScreen
import com.example.purrsistence.ui.screens.HomeScreen
import com.example.purrsistence.ui.screens.ProfileScreen
import com.example.purrsistence.ui.screens.ShopScreen
import com.example.purrsistence.ui.screens.StatisticsScreen
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.screens.TrackingScreen
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    trackingViewModel: TrackingViewModel,
    statisticsViewModel: StatisticsViewModel,
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
            userViewModel = userViewModel,
            goalViewModel = goalViewModel,
            onStartTracking = { goalId, userId, deepFocus ->
                trackingViewModel.startTrack(goalId, userId, deepFocus)
            }
        ) }
        // GOALS
        composable("goals") {
            GoalsScreen(
                userViewModel = userViewModel,
                goalViewModel = goalViewModel,
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
                viewModel = goalViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        // -> add goal
        composable("add_goal") {
            AddGoalScreen(
                onSave = { title, type, minutes, deepFocus ->
                    goalViewModel.addGoal(
                        userId = userViewModel.currentUserId,
                        title = title,
                        type = type,
                        weeklyMinutes = minutes,
                        deepFocus = deepFocus,
                        inactive = false, // inactive false per default
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
        // STATISTICS
        composable("statistics") {
            StatisticsScreen(
                viewModel = statisticsViewModel,
            )
        }
        // SHOP
        composable("shop") {
            ShopScreen(
                userViewModel = userViewModel
            )
        }
        // PROFILE
        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel
            )
        }
    }
}