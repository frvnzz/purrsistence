package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.purrsistence.ui.components.BottomNavBar
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.components.TutorialOverlay
import com.example.purrsistence.ui.components.TutorialStep
import com.example.purrsistence.ui.navigation.AppNavHost
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.util.SoundManager
import com.example.purrsistence.ui.viewmodel.FriendViewModel
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
    friendViewModel: FriendViewModel,
    openTrackingFromNotification: Boolean,
    onTrackingNotificationHandled: () -> Unit

) {
    // remember states
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarState = remember { mutableStateOf(TopBarState()) }

    var roomViewCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var currencyBadgeCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var selectCatsCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var startButtonCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var bottomNavCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val tutorialCompleted by userViewModel.tutorialCompleted.collectAsState()
    val tutorialStepIndex by userViewModel.tutorialStepIndex.collectAsState()

    val tutorialSteps = remember(roomViewCoords, currencyBadgeCoords, selectCatsCoords, startButtonCoords, bottomNavCoords) {
        val navOptions: NavOptionsBuilder.() -> Unit = {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }

        listOf(
            TutorialStep(
                title = "Welcome to Purrsistence!",
                description = "Let's take a quick tour of how to stay focused with your feline friends."
            ),
            TutorialStep(
                title = "Navigation",
                description = "Use the bottom bar to switch between Home, Goals, Shop, and more.",
                targetCoordinates = bottomNavCoords,
                onEnter = { if (navController.currentDestination?.route != "home") navController.navigate("home", navOptions) }
            ),
            TutorialStep(
                title = "Your Room",
                description = "This is where your cats hang out while you focus!",
                targetCoordinates = roomViewCoords,
                onEnter = { if (navController.currentDestination?.route != "home") navController.navigate("home", navOptions) }
            ),
            TutorialStep(
                title = "Fish Earnings",
                description = "Earn fish by completing focus sessions. Use them in the shop!",
                targetCoordinates = currencyBadgeCoords,
                onEnter = { if (navController.currentDestination?.route != "home") navController.navigate("home", navOptions) }
            ),
            TutorialStep(
                title = "Your Goals",
                description = "View and manage all your focus goals here.",
                onEnter = { if (navController.currentDestination?.route != "goals") navController.navigate("goals", navOptions) }
            ),
            TutorialStep(
                title = "The Shop",
                description = "Spend your hard-earned fish to collect more cats!",
                onEnter = { if (navController.currentDestination?.route != "shop") navController.navigate("shop", navOptions) }
            ),
            TutorialStep(
                title = "Statistics",
                description = "Track your focus progress over time.",
                onEnter = { if (navController.currentDestination?.route != "statistics") navController.navigate("statistics", navOptions) }
            ),
            TutorialStep(
                title = "Select Cats",
                description = "Choose which cats appear in your room from your collection.",
                targetCoordinates = selectCatsCoords,
                onEnter = { if (navController.currentDestination?.route != "home") navController.navigate("home", navOptions) }
            ),
            TutorialStep(
                title = "Ready to start?",
                description = "Select a goal and tap the play button on the Home screen to start your journey!",
                targetCoordinates = startButtonCoords,
                onEnter = { if (navController.currentDestination?.route != "home") navController.navigate("home", navOptions) }
            )
        )
    }

    val currentTutorialStep = tutorialSteps.getOrNull(tutorialStepIndex)

    LaunchedEffect(tutorialStepIndex, tutorialCompleted) {
        if (!tutorialCompleted) {
            // Clear screen-specific coordinates when moving to a new step
            // This prevents "ghost" spotlights during transitions
            roomViewCoords = null
            currencyBadgeCoords = null
            selectCatsCoords = null
            startButtonCoords = null

            currentTutorialStep?.onEnter?.invoke()
        }
    }

    val context = LocalContext.current
    val soundManager = remember(context) {
        SoundManager(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    // Check the user state (remote supabase signed in or out)
    LaunchedEffect(userViewModel.isSupabaseSignedIn.collectAsState().value) {
        if (userViewModel.isSupabaseSignedIn.value) {
            userViewModel.syncFromSupabase()
        }
    }

    LaunchedEffect(openTrackingFromNotification) {
        if (openTrackingFromNotification) {
            navController.navigate("tracking") {
                launchSingleTop = true
                restoreState = true
            }
            onTrackingNotificationHandled()
        }
    }

    // open ShopScreen (for onClick CurrencyBadge in TopBar)
    val openShop: () -> Unit = {
        navController.navigate("shop") {
            // avoid building up a large stack of destinations (backstack)
            // so you can always navigate back to the start destination via NavBar (e.g. home)
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    val topLevelRoutes = listOf("statistics", "goals", "home", "shop", "profile")

    val routesWithoutTopBar = listOf("tracking", "rewards")

    Box(modifier = Modifier.fillMaxSize()) {
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
                    BottomNavBar(
                        navController = navController,
                        onStatisticsClick = { statisticsViewModel.jumpToThisWeek() },
                        onPositioned = { bottomNavCoords = it }
                    )
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
                friendViewModel = friendViewModel,
                openShop = openShop,
                modifier = Modifier.padding(padding),
                snackbarHostState = snackbarHostState,
                soundManager = soundManager,
                onRoomViewPositioned = { roomViewCoords = it },
                onCurrencyBadgePositioned = { currencyBadgeCoords = it },
                onSelectCatsPositioned = { selectCatsCoords = it },
                onStartButtonPositioned = { startButtonCoords = it }
            )
        }

    val completeTutorial = {
        userViewModel.completeTutorial()
        if (navController.currentDestination?.route != "home") {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (!tutorialCompleted) {
        TutorialOverlay(
            currentStep = currentTutorialStep,
            isLastStep = tutorialStepIndex == tutorialSteps.size - 1,
            onNext = {
                if (tutorialStepIndex < tutorialSteps.size - 1) {
                    userViewModel.nextTutorialStep()
                } else {
                    completeTutorial()
                }
            },
            onSkip = completeTutorial
        )
    }
    }
}
