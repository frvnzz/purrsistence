package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.purrsistence.ui.components.BottomNavBar
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.navigation.AppNavHost
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.util.SoundManager
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun MainScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    trackingViewModel: TrackingViewModel,
    statisticsViewModel: StatisticsViewModel,
) {
    // remember states
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarState = remember { mutableStateOf(TopBarState()) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    // Check the user state (remote supabase signed in or out)
    LaunchedEffect(Unit) {
        if (userViewModel.isSupabaseSignedIn.value) {
            userViewModel.syncFromSupabase()
        }
    }

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    val topLevelRoutes = listOf("statistics", "goals", "home", "shop", "profile")

    val routesWithoutTopBar = listOf("tracking", "rewards")

    Scaffold(
        // SNACK BAR (for alerts / warnings / errors)
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            }
        },
        // TOP BAR
        topBar = {
            // show TopBar only in screens where it's needed
            if (currentRoute !in routesWithoutTopBar) {
                TopBar(
                    title = topBarState.value.title,
                    actions = topBarState.value.actions,
                    onBackClick = topBarState.value.onBackClick
                )
            }
        },
        // NAV BAR
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        AppNavHost(
            setTopBar = { topBarState.value = it },
            navController = navController,
            userViewModel = userViewModel,
            goalViewModel = goalViewModel,
            trackingViewModel = trackingViewModel,
            statisticsViewModel = statisticsViewModel,
            modifier = Modifier.padding(padding),
            snackbarHostState = snackbarHostState,
            soundManager = soundManager
        )
    }
}