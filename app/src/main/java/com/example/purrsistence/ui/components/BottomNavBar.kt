package com.example.purrsistence.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    // Determine the current route to know which tab is selected
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // list of all possible tab routes from AppNavHost
    // TODO: use single source of truth for routes (screen model) in the future
    val items = listOf("home", "goals")

    NavigationBar {
        items.forEach { screen ->
            val isSelected = currentRoute == screen

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen) {
                        // make sure that only one tab is selected at a time, and back navigates to home (startDestination)
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        // Display the icon for the current screen (filled when tab is selected)
                        imageVector = when (screen) {
                            "home" -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                            "goals" -> if (isSelected) Icons.Filled.AssignmentTurnedIn else Icons.Outlined.AssignmentTurnedIn
                            else -> Icons.Default.Home
                        },
                        contentDescription = screen
                    )
                },
                label = { Text(screen.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}