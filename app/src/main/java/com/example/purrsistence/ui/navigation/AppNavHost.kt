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
import com.example.purrsistence.ui.screens.AuthScreen
import com.example.purrsistence.ui.screens.EditGoalScreen
import com.example.purrsistence.ui.screens.FriendProfileScreen
import com.example.purrsistence.ui.screens.GoalDetailsScreen
import com.example.purrsistence.ui.screens.GoalsScreen
import com.example.purrsistence.ui.screens.HomeScreen
import com.example.purrsistence.ui.screens.ProfileScreen
import com.example.purrsistence.ui.screens.ShopScreen
import com.example.purrsistence.ui.screens.StatisticsScreen
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.screens.SettingsScreen
import com.example.purrsistence.ui.screens.FriendsScreen
import com.example.purrsistence.ui.screens.FriendSearchScreen
import com.example.purrsistence.ui.screens.RewardsScreen
import com.example.purrsistence.ui.screens.TrackingScreen
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import com.example.purrsistence.ui.viewmodel.FriendViewModel

@Composable
fun AppNavHost(
    setTopBar: (TopBarState) -> Unit,
    navController: NavHostController,
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    trackingViewModel: TrackingViewModel,
    statisticsViewModel: StatisticsViewModel,
    friendViewModel: FriendViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
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
        modifier = modifier
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
                setTopBar = setTopBar
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
        composable("goalDetails/{goalId}") { backStackEntry ->

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
                setTopBar = setTopBar
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
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
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
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        // TRACKING
        composable("tracking") {
            TrackingScreen(
                viewModel = trackingViewModel
            )
        }
        composable("rewards") {
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
                setTopBar = setTopBar
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
                        navController.navigate("auth")
                    }
                }
            )
        }
        // SETTINGS
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        // FRIENDS
        composable("friends") {
            FriendsScreen(
                viewModel = friendViewModel,
                onAddFriendClick = {
                    navController.navigate("friend_search")
                },
                onFriendClick = { friendUserId ->
                    navController.navigate("friend_profile/$friendUserId")
                },
                onBack = {
                    navController.popBackStack()
                },
                setTopBar = setTopBar
            )
        }
        composable("friend_search") {
            FriendSearchScreen(
                viewModel = friendViewModel,
                onBack = { navController.popBackStack() },
                setTopBar = setTopBar
            )
        }
        composable("friend_profile/{friendUserId}") { backStackEntry ->
            val friendUserId = backStackEntry.arguments
                ?.getString("friendUserId")

            if (friendUserId != null) {
                FriendProfileScreen(
                    viewModel = friendViewModel,
                    friendUserId = friendUserId,
                    onBack = {
                        friendViewModel.clearSelectedFriendProfile()
                        navController.popBackStack()
                    },
                    setTopBar = setTopBar
                )
            }
        }

        composable("auth") {
            AuthScreen(
                userViewModel = userViewModel,
                setTopBar = setTopBar,
                onAuthSuccess = {
                    navController.navigate("friends") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}