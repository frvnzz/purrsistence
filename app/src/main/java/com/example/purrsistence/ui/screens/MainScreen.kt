package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.components.BottomNavBar
import com.example.purrsistence.ui.navigation.AppNavHost

@Composable
fun MainScreen(
    viewModel: DataViewModel
) {
    val navController = rememberNavController()

    // find out which route is currently active
    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    // main routes that are shown on BottomNavBar
    val topLevelRoutes = listOf("home", "goals")

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            AppNavHost(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}