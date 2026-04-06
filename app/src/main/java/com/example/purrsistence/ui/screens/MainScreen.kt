package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.components.BottomNavBar
import com.example.purrsistence.ui.navigation.AppNavHost
import com.example.purrsistence.ui.tracking.TrackingViewModel

@Composable
fun MainScreen(
    dataViewModel: DataViewModel,
    trackingViewModel: TrackingViewModel
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    val topLevelRoutes = listOf("home", "goals")

    Scaffold(
        // SNACK BAR (for alerts / warnings / errors)
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary, // background
                    contentColor = MaterialTheme.colorScheme.onPrimary  // text color
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
            navController = navController,
            dataViewModel = dataViewModel,
            trackingViewModel = trackingViewModel,
            modifier = Modifier.padding(padding),
            snackbarHostState = snackbarHostState
        )
    }
}