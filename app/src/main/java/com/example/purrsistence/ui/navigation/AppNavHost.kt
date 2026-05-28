package com.example.purrsistence.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.purrsistence.ui.screens.AddGoalScreen
import com.example.purrsistence.ui.screens.AuthScreen
import com.example.purrsistence.ui.screens.EditGoalScreen
import com.example.purrsistence.ui.screens.FriendsScreen
import com.example.purrsistence.ui.screens.GoalDetailsScreen
import com.example.purrsistence.ui.screens.GoalsScreen
import com.example.purrsistence.ui.screens.HomeScreen
import com.example.purrsistence.ui.screens.ProfileScreen
import com.example.purrsistence.ui.screens.RewardsScreen
import com.example.purrsistence.ui.screens.SettingsScreen
import com.example.purrsistence.ui.screens.ShopScreen
import com.example.purrsistence.ui.screens.StatisticsScreen
import com.example.purrsistence.ui.screens.TrackingScreen
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.util.SoundManager
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun AppNavHost(
    setTopBar: (TopBarState) -> Unit,
    navController: NavHostController,
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    trackingViewModel: TrackingViewModel,
    statisticsViewModel: StatisticsViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    soundManager: SoundManager
) {
    LaunchedEffect(Unit) {
        trackingViewModel.events.collect { event ->
            when (event) {

                TrackingEvent.NavigateToTrackingScreen -> {
                    navController.navigate("tracking")
                }

                TrackingEvent.NavigateToRewardsScreen -> {
                    navController.navigate("rewards")
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
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
        popEnterTransition = { fadeIn(animationSpec = tween(250)) },
        popExitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        // all screens go here :)
        // TODO: Maybe replace with single source of truth for routes (screen model) in the future

        // HOME
        composable("home") {
            HomeScreen(
                userViewModel = userViewModel,
                goalViewModel = goalViewModel,
                onStartTracking = { goalId, goalTitle, userId, deepFocus ->
                    trackingViewModel.startTrack(
                        goalId = goalId,
                        goalTitle = goalTitle,
                        userId = userId,
                        deepFocus = deepFocus
                    )
                },
                setTopBar = setTopBar,
                soundManager = soundManager
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
                    navController.navigate("goalDetails/$goalId")
                },
                snackbarHostState = snackbarHostState,
                setTopBar = setTopBar
            )
        }
        // -> Goal Details
        composable(
            "goalDetails/{goalId}",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Left) },
            popEnterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Right) },
            popExitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) { backStackEntry ->

            val goalId = backStackEntry.arguments
                ?.getString("goalId")
                ?.toIntOrNull()

            GoalDetailsScreen(
                goalId = goalId,
                goalViewModel = goalViewModel,
                onEditClick = { selectedGoalId ->
                    navController.navigate("edit_goal/$selectedGoalId")
                },
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar,
                onStartTracking = { trackingGoalId, goalTitle, userId, deepFocus ->
                    trackingViewModel.startTrack(
                        goalId = trackingGoalId,
                        goalTitle = goalTitle,
                        userId = userId,
                        deepFocus = deepFocus
                    )
                }
            )
        }

        // -> edit goal
        composable(
            "edit_goal/{goalId}",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Left) },
            popEnterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Right) },
            popExitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments
                ?.getString("goalId")
                ?.toInt()

            EditGoalScreen(
                goalId = goalId,
                viewModel = goalViewModel,
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        // -> add goal
        composable(
            "add_goal",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Left) },
            popEnterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Right) },
            popExitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) {
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
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        // TRACKING
        composable(
            "tracking",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Up) },
            exitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Down) }
        ) {
            TrackingScreen(
                viewModel = trackingViewModel
            )
        }
        composable(
            "rewards",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            RewardsScreen(
                viewModel = trackingViewModel,
                onReturnHome = {
                    trackingViewModel.returnHome()
                }
            )
        }
        // STATISTICS
        composable("statistics") {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                setTopBar = setTopBar
            )
        }
        // SHOP
        composable("shop") {
            ShopScreen(
                userViewModel = userViewModel,
                setTopBar = setTopBar,
                soundManager = soundManager
            )
        }
        // PROFILE
        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                setTopBar = setTopBar,
                onNavigateToSettings = { navController.navigate("settings") },
                // navigate to friendsScreen or authScreen depending on the remote user state (signed out or in)
                onNavigateToFriends = {
                    if (userViewModel.isSupabaseSignedIn.value) {
                        navController.navigate("friends")
                    } else {
                        navController.navigate("auth?redirect=friends")
                    }
                }
            )
        }
        // SETTINGS
        composable(
            "settings",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = {
                if (targetState.destination.route?.startsWith("auth") == true) {
                    fadeOut(animationSpec = tween(300))
                } else {
                    slideOut(AnimatedContentTransitionScope.SlideDirection.Left)
                }
            },
            popEnterTransition = {
                if (initialState.destination.route?.startsWith("auth") == true) {
                    fadeIn(animationSpec = tween(300))
                } else {
                    slideIn(AnimatedContentTransitionScope.SlideDirection.Right)
                }
            },
            popExitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) {
            SettingsScreen(
                userViewModel = userViewModel,
                onNavigateToAuth = { navController.navigate("auth") },
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        // FRIENDS
        composable(
            "friends",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = {
                if (targetState.destination.route?.startsWith("auth") == true) {
                    fadeOut(animationSpec = tween(300))
                } else {
                    slideOut(AnimatedContentTransitionScope.SlideDirection.Left)
                }
            },
            popEnterTransition = {
                if (initialState.destination.route?.startsWith("auth") == true) {
                    fadeIn(animationSpec = tween(300))
                } else {
                    slideIn(AnimatedContentTransitionScope.SlideDirection.Right)
                }
            },
            popExitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        composable(
            "auth?redirect={redirect}",
            enterTransition = { slideIn(AnimatedContentTransitionScope.SlideDirection.Up) },
            exitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Down) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOut(AnimatedContentTransitionScope.SlideDirection.Down) }
        ) { backStackEntry ->
            val redirect = backStackEntry.arguments?.getString("redirect")
            AuthScreen(
                userViewModel = userViewModel,
                setTopBar = setTopBar,
                onAuthSuccess = {
                    if (redirect != null) {
                        navController.navigate(redirect) {
                            popUpTo("auth?redirect={redirect}") {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

//helper functions common transitions
private fun AnimatedContentTransitionScope<*>.slideIn(
    direction: AnimatedContentTransitionScope.SlideDirection
) = slideIntoContainer(
    towards = direction,
    animationSpec = tween(400)
)

private fun AnimatedContentTransitionScope<*>.slideOut(
    direction: AnimatedContentTransitionScope.SlideDirection
) = slideOutOfContainer(
    towards = direction,
    animationSpec = tween(400)
)
